package com.ml.arunneo.docqa.domain.llm
import android.util.Log
import android.app.Application
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GemmaLocalAPI constructor(private val application: Application) {

    var gemmaInference = GemmaInference.getInstance(application.applicationContext)

    init {
        // Here's a good reference on topK, topP and temperature
        // parameters, which are used to control the output of a LLM
        // See
        // https://ivibudh.medium.com/a-guide-to-controlling-llm-model-output-exploring-top-k-top-p-and-temperature-parameters-ed6a31313910
        //var gemmaInference = GemmaInference.getInstance(application.applicationContext)
    }

    suspend fun getResponse(prompt: String): String? =
        withContext(Dispatchers.IO) {
            Log.e("APP", "Prompt given: $prompt")
            val response = gemmaInference.getResponse(prompt)
            return@withContext response
        }
}
