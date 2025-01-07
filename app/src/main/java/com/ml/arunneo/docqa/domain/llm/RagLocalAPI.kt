package com.ml.arunneo.docqa.domain.llm
import android.util.Log
import android.app.Application
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.aiedge.rag.models.AsyncProgressListener
import com.google.aiedge.rag.models.LanguageModelResponse
import com.ml.arunneo.docqa.data.QueryResult
import com.ml.arunneo.docqa.data.RetrievedContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher

class RagLocalAPI constructor(private val application: Application) {

    var ragPipeline = RagPipeline.getInstance(application.applicationContext)
    private val backgroundExecutor: Executor = Executors.newSingleThreadExecutor()

    init {
        // Here's a good reference on topK, topP and temperature
        // parameters, which are used to control the output of a LLM
        // See
        // https://ivibudh.medium.com/a-guide-to-controlling-llm-model-output-exploring-top-k-top-p-and-temperature-parameters-ed6a31313910
        //var gemmaInference = GemmaInference.getInstance(application.applicationContext)
    }

    suspend fun getResponse(prompt: String): String? =
        withContext(backgroundExecutor.asCoroutineDispatcher())  {
            val retrievedContextList = ArrayList<RetrievedContext>()
            Log.e("APP", "Prompt given: $prompt")
            val response = ragPipeline.generateResponse(
                prompt,
                object : AsyncProgressListener<LanguageModelResponse> {
                    override fun run(response: LanguageModelResponse, done: Boolean) {
                       QueryResult(response.text, retrievedContextList)
                    }
                },
            )
            return@withContext response
        }


    suspend fun addChunk(chunks: List<String>)=
        withContext(Dispatchers.IO) {

            Log.e("APP", "Prompt given: ${chunks.size}")
            ragPipeline.memorize(chunks)
        }
}
