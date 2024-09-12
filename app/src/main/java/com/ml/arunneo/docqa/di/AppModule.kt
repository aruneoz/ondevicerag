package com.ml.arunneo.docqa.di

/*
@Time    : 2024/09/01 17:45
@Author  : asanthan
@File    : AppModule.kt
@Descriptor: This is the Ondevice LLM Demonstration with Gemma and Media Pipe
*/

import android.app.Application
import com.ml.arunneo.docqa.data.ChunksDB
import com.ml.arunneo.docqa.data.DocumentsDB
import com.ml.arunneo.docqa.domain.embeddings.SentenceEmbeddingProvider
import com.ml.arunneo.docqa.domain.llm.GeminiRemoteAPI
import com.ml.arunneo.docqa.domain.llm.GemmaLocalAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// AppModule provides dependencies that are to be injected by Hilt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // SingletonComponent ensures that instances survive
    // across the application's lifespan
    // @Singleton creates a single instance in the app's lifespan

    @Provides
    @Singleton
    fun provideDocumentsDB(): DocumentsDB {
        return DocumentsDB()
    }

    @Provides
    @Singleton
    fun provideChunksDB(): ChunksDB {
        return ChunksDB()
    }

//    @Provides
//    @Singleton
//    fun provideChunksDB(): ChunksDBL {
//        return ChunksDBL()
//    }

    @Provides
    @Singleton
    fun provideGeminiRemoteAPI(): GeminiRemoteAPI {
        return GeminiRemoteAPI()
    }

    @Provides
    @Singleton
    fun provideGemmaLocalAPI(context: Application): GemmaLocalAPI {
        return GemmaLocalAPI(context)
    }

    @Provides
    @Singleton
    fun provideSentenceEncoder(context: Application): SentenceEmbeddingProvider {
        return SentenceEmbeddingProvider(context)
    }
}
