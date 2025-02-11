package com.ml.arunneo.docqa.domain.llm

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.aiedge.rag.chains.ChainConfig
import com.google.aiedge.rag.chains.RetrievalAndInferenceChain
import com.google.aiedge.rag.memory.DefaultSemanticTextMemory
import com.google.aiedge.rag.memory.SqliteVectorStore
import com.google.aiedge.rag.models.AsyncProgressListener
import com.google.aiedge.rag.models.Embedder
import com.google.aiedge.rag.models.GeckoEmbeddingModel
import com.google.aiedge.rag.models.GeminiEmbedder
import com.google.aiedge.rag.models.LanguageModelResponse
import com.google.aiedge.rag.models.MediaPipeLanguageModel
import com.google.aiedge.rag.prompt.PromptBuilder
import com.google.aiedge.rag.retrieval.RetrievalConfig
import com.google.aiedge.rag.retrieval.RetrievalConfig.TaskType
import com.google.aiedge.rag.retrieval.RetrievalRequest
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Optional
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull


class RagPipeline constructor(application: Context) {
    private val mediaPipeLanguageModelOptions: LlmInferenceOptions =
        LlmInferenceOptions.builder().setModelPath(
            GEMMA_MODEL_PATH
        ).build()
    private val mediaPipeLanguageModel: MediaPipeLanguageModel =
        MediaPipeLanguageModel(application.applicationContext, mediaPipeLanguageModelOptions)

    private val embedder: Embedder<String> = if (COMPUTE_EMBEDDINGS_LOCALLY) {
        GeckoEmbeddingModel(
            GECKO_MODEL_PATH,
            Optional.of(TOKENIZER_MODEL_PATH),
            USE_GPU_FOR_EMBEDDINGS,
        )
    } else {
        GeminiEmbedder(
            GEMINI_EMBEDDING_MODEL,
            GEMINI_API_KEY
        )
    }

    private val config = ChainConfig.create(
        mediaPipeLanguageModel, PromptBuilder(QA_PROMPT_TEMPLATE2),
        DefaultSemanticTextMemory(
            SqliteVectorStore(768), embedder
        )
    )


    private val retrievalAndInferenceChain = RetrievalAndInferenceChain(config)


    //val MODEL_PATH = "/data/local/tmp/llm/model.bin.gpu.bin"
    init {
            mediaPipeLanguageModel.initialize()
         }

    fun memorizeChunks(context: Context, filename: String) {
        // BufferedReader is needed to read the *.txt file
        // Create and Initialize BufferedReader
        val reader = BufferedReader(InputStreamReader(context.assets.open(filename)))

        val sb = StringBuilder()
        val texts = mutableListOf<String>()
        generateSequence { reader.readLine() }
            .forEach { line ->
                if (line.startsWith(CHUNK_SEPARATOR)) {
                    if (sb.isNotEmpty()) {
                        val chunk = sb.toString()
                        Log.i("MemorizeChunks", chunk)
                        texts.add(chunk)
                        if (texts.size >= 100) {
                            memorize(texts)
                            texts.clear()
                        }
                    }
                    sb.clear()
                    sb.append(line.removePrefix(CHUNK_SEPARATOR).trim())
                } else {
                    sb.append(" ")
                    sb.append(line)
                }
            }
        if (sb.isNotEmpty()) {
            texts.add(sb.toString())
        }
        if (texts.isNotEmpty()) {
            memorize(texts)
        }
        reader.close()
    }

    /** Stores input texts in the semantic text memory. */
    fun memorize(facts: List<String>) {
        config.semanticMemory.getOrNull()?.recordBatchedMemoryItems(ImmutableList.copyOf(facts))
        print("*******Facts**********")
        print(facts.toString())
        print("*******Facts**********")
    }


    /** Generates the response from the LLM. */
    suspend fun generateResponse(
        prompt: String,
        callback: AsyncProgressListener<LanguageModelResponse>?
    ): String =
        coroutineScope {
            val retrievalRequest =
                RetrievalRequest.create(
                    prompt,
                    RetrievalConfig.create(2, 0.0f, TaskType.QUESTION_ANSWERING)
                )

            Log.i("Retrieval Request", retrievalRequest.toString())

            retrievalAndInferenceChain.invoke(retrievalRequest, callback).await().text
        }

    companion object {
        // NB: Make sure the filename is *unique* per model you use!
        // Weight caching is currently based on filename alone.
        private const val COMPUTE_EMBEDDINGS_LOCALLY = true
        private const val USE_GPU_FOR_EMBEDDINGS = true
        private const val CHUNK_SEPARATOR = "\n"
        private const val GEMMA_MODEL_PATH = "/data/local/tmp/llm/gemma2-2b-it-gpu-int8.bin"
        private const val TOKENIZER_MODEL_PATH = "/data/local/tmp/sentencepiece.model"
        private const val GECKO_MODEL_PATH = "/data/local/tmp/gecko.tflite"
        private const val GEMINI_EMBEDDING_MODEL = "models/text-embedding-004"
        private const val GEMINI_API_KEY = "..."
        private const val QA_PROMPT_TEMPLATE1: String =
            "You are an intelligent search engine. You will be provided with retrieved context, as well as the users query. Your job is to understand the user query, and answer based on the retrieved context below. Think step by step and answer must be relevant and based on the only the retrieved context. Ensure your answer is logical and uses professional language. Here is the retrieved context -------------------------------------------------- {0} -------------------------------------------------- , i want you to answer User's query {1} <ctrl23>"
        private const val QA_PROMPT_TEMPLATE2: String =
            "You are an assistant for question-answering tasks. Use the retrieved context to answer the question. If you don't know the answer, just say that you don't know. Use three sentences maximum and keep the answer concise.\n Question: {1} \n Context: {0} \n Answer:<ctrl23>"
        private var instance: RagPipeline? = null



        fun getInstance(context: Context): RagPipeline {
            return if (instance != null) {
                instance!!
            } else {
                RagPipeline(context).also { instance = it }
            }
        }


    }



}




///** The RAG pipeline for LLM generation. */
//
//class RagPipeline
//
//@Inject
//public constructor(val application: Application) {
//    private val mediaPipeLanguageModelOptions: LlmInferenceOptions =
//        LlmInferenceOptions.builder().setModelPath(
//            GEMMA_MODEL_PATH
//        ).build()
//    private val mediaPipeLanguageModel: MediaPipeLanguageModel =
//        MediaPipeLanguageModel(application.applicationContext, mediaPipeLanguageModelOptions)
//
//    private val embedder: Embedder<String> = if (COMPUTE_EMBEDDINGS_LOCALLY) {
//        GeckoEmbeddingModel(
//            GECKO_MODEL_PATH,
//            Optional.of(TOKENIZER_MODEL_PATH),
//            USE_GPU_FOR_EMBEDDINGS,
//        )
//    } else {
//        GeminiEmbedder(
//            GEMINI_EMBEDDING_MODEL,
//            GEMINI_API_KEY
//        )
//    }
//
//    private val config = ChainConfig.create(
//        mediaPipeLanguageModel, PromptBuilder(QA_PROMPT_TEMPLATE1),
//        DefaultSemanticTextMemory(
//            SqliteVectorStore(768), embedder
//        )
//    )
//    private val retrievalAndInferenceChain = RetrievalAndInferenceChain(config)
//
//    fun init () {
//        Futures.addCallback(
//            mediaPipeLanguageModel.initialize(),
//            object : FutureCallback<Boolean> {
//                override fun onSuccess(result: Boolean) {
//                    // no-op
//                }
//
//                override fun onFailure(t: Throwable) {
//                    // no-op
//                }
//            },
//            Executors.newSingleThreadExecutor(),
//        )
//    }
//
//    fun memorizeChunks(context: Context, filename: String) {
//        // BufferedReader is needed to read the *.txt file
//        // Create and Initialize BufferedReader
//        val reader = BufferedReader(InputStreamReader(context.assets.open(filename)))
//
//        val sb = StringBuilder()
//        val texts = mutableListOf<String>()
//        generateSequence { reader.readLine() }
//            .forEach { line ->
//                if (line.startsWith(CHUNK_SEPARATOR)) {
//                    if (sb.isNotEmpty()) {
//                        val chunk = sb.toString()
//                        Log.i("MemorizeChunks", chunk)
//                        texts.add(chunk)
//                        if (texts.size >= 100) {
//                            memorize(texts)
//                            texts.clear()
//                        }
//                    }
//                    sb.clear()
//                    sb.append(line.removePrefix(CHUNK_SEPARATOR).trim())
//                } else {
//                    sb.append(" ")
//                    sb.append(line)
//                }
//            }
//        if (sb.isNotEmpty()) {
//            texts.add(sb.toString())
//        }
//        if (texts.isNotEmpty()) {
//            memorize(texts)
//        }
//        reader.close()
//    }
//
//    /** Stores input texts in the semantic text memory. */
//    fun memorize(facts: List<String>) {
//        config.semanticMemory.getOrNull()?.recordBatchedMemoryItems(ImmutableList.copyOf(facts))
//    }
//
//    /** Generates the response from the LLM. */
//    suspend fun generateResponse(
//        prompt: String,
//        callback: AsyncProgressListener<LanguageModelResponse>?
//    ): String =
//        coroutineScope {
//            val retrievalRequest =
//                RetrievalRequest.create(
//                    prompt,
//                    RetrievalConfig.create(2, 0.0f, TaskType.QUESTION_ANSWERING)
//                )
//            retrievalAndInferenceChain.invoke(retrievalRequest, callback).await().text
//        }
//
//    companion object {
//        private const val COMPUTE_EMBEDDINGS_LOCALLY = true
//        private const val USE_GPU_FOR_EMBEDDINGS = true
//        private const val CHUNK_SEPARATOR = "\n"
//        private const val GEMMA_MODEL_PATH = "/data/local/tmp/llm/gemma2-2b-it-gpu-int8.bin"
//        private const val TOKENIZER_MODEL_PATH = "/data/local/tmp/sentencepiece.model"
//        private const val GECKO_MODEL_PATH = "/data/local/tmp/gecko.tflite"
//        private const val GEMINI_EMBEDDING_MODEL = "models/text-embedding-004"
//        private const val GEMINI_API_KEY = "..."
//        private const val QA_PROMPT_TEMPLATE1: String =
//            "You are an assistant for question-answering tasks. Here are the things I want to remember: {0} Use the things I want to remember, answer the following question the user has: {1} <ctrl23>"
//    }
//}