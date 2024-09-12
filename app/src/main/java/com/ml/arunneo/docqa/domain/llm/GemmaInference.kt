package com.ml.arunneo.docqa.domain.llm

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class GemmaInference private constructor(context: Context) {
    private var llmInference: LlmInference

    private val modelExists: Boolean
        get() = File(MODEL_PATH).exists()

    private val _partialResults = MutableSharedFlow<Pair<String, Boolean>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val partialResults: SharedFlow<Pair<String, Boolean>> = _partialResults.asSharedFlow()
    //val MODEL_PATH = "/data/local/tmp/llm/gemma-2b-cpu-int8.task"
    init {
        if (!modelExists) {
            throw IllegalArgumentException("Model not found at path: $MODEL_PATH")
        }

        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(MODEL_PATH)
            .setMaxTokens(1024)
//            .setResultListener { partialResult, done ->
//                _partialResults.tryEmit(partialResult to done)
//            }
            .build()



        llmInference = LlmInference.createFromOptions(context, options)
    }

    suspend fun getResponse(prompt: String): String? =
        withContext(Dispatchers.IO) {
            Log.e("APP", "Prompt given: $prompt")
            val response = llmInference.generateResponse(prompt)
            return@withContext response
        }

    companion object {
        // NB: Make sure the filename is *unique* per model you use!
        // Weight caching is currently based on filename alone.
        private const val MODEL_PATH = "/data/local/tmp/llm/model.bin.gpu.bin"
        private var instance: GemmaInference? = null
             fun getInstance(context: Context): GemmaInference {
            return if (instance != null) {
                instance!!
            } else {
                GemmaInference(context).also { instance = it }
            }
        }
    }
}




//class GemmaLocalAPI {
//
//    private val apiKey = BuildConfig.geminiKey
//    private val generativeModel: GenerativeModel
//
//    init {
//        // Here's a good reference on topK, topP and temperature
//        // parameters, which are used to control the output of a LLM
//        // See
//        // https://ivibudh.medium.com/a-guide-to-controlling-llm-model-output-exploring-top-k-top-p-and-temperature-parameters-ed6a31313910
//        val configBuilder = GenerationConfig.Builder()
//        configBuilder.topP = 0.4f
//        configBuilder.temperature = 0.3f
//        generativeModel =
//            GenerativeModel(
//                modelName = "gemini-1.5-flash",
//                apiKey = apiKey,
//                generationConfig = configBuilder.build()
//            )
//    }
//
//    suspend fun getResponse(prompt: String): String? =
//        withContext(Dispatchers.IO) {
//            Log.e("APP", "Prompt given: $prompt")
//            val response = generativeModel.generateContent(prompt)
//            return@withContext response.text
//        }
//}
