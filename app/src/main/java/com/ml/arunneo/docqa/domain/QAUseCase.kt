package com.ml.arunneo.docqa.domain

/*
@Time    : 2024/09/01 17:45
@Author  : asanthan
@File    : QAUseCase.kt
@Descriptor: This is the Ondevice LLM Demonstration with Gemma and Media Pipe
*/

import android.util.Log
import androidx.collection.emptyLongSet
import com.ml.arunneo.docqa.data.QueryResult
import com.ml.arunneo.docqa.data.RetrievedContext
import com.ml.arunneo.docqa.domain.llm.GeminiRemoteAPI
import com.ml.arunneo.docqa.domain.llm.GemmaLocalAPI
import com.google.aiedge.rag.models.LanguageModelResponse
import com.google.aiedge.rag.models.AsyncProgressListener
import com.ml.arunneo.docqa.domain.llm.RagLocalAPI
import com.ml.arunneo.docqa.domain.llm.RagPipeline
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class QAUseCase
@Inject
constructor(
    private val documentsUseCase: DocumentsUseCase,
    private val chunksUseCase: ChunksUseCase,
    private val geminiRemoteAPI: GeminiRemoteAPI,
    private val gemmaLocalAPI: GemmaLocalAPI,
    private val ragLocalAPI: RagLocalAPI,


) {

    fun getAnswer(query: String, prompt: String, onResponse: ((QueryResult) -> Unit)) {
        var jointContext = ""
        val retrievedContextList = ArrayList<RetrievedContext>()
//        chunksUseCase.getSimilarChunks(query, n = 4).forEach {
//            jointContext += " " + it.second.chunkData
//            retrievedContextList.add(RetrievedContext(it.second.docFileName, it.second.chunkData))
//        }

        val inputPrompt = prompt.replace("\$QUERY", query)
        Log.e("APP", "Context: $inputPrompt")
        CoroutineScope(Dispatchers.IO).launch {

            ragLocalAPI.getResponse(query)?.let { llmResponse ->
                onResponse(QueryResult(llmResponse, retrievedContextList))
            }

//            gemmaLocalAPI.getResponse(inputPrompt)?.let { llmResponse ->
//                onResponse(QueryResult(llmResponse, retrievedContextList))
//            }
        }


    }

    fun canGenerateAnswers(): Boolean {
        return documentsUseCase.getDocsCount() > 0
    }
}
