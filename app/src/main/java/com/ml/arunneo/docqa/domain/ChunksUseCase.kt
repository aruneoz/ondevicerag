package com.ml.arunneo.docqa.domain

import android.util.Log
import com.ml.arunneo.docqa.data.Chunk
import com.ml.arunneo.docqa.data.ChunksDB
import com.ml.arunneo.docqa.domain.embeddings.SentenceEmbeddingProvider
import dev.langchain4j.data.segment.TextSegment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChunksUseCase
@Inject
constructor(private val chunksDB: ChunksDB, private val sentenceEncoder: SentenceEmbeddingProvider) {

    fun addChunk(docId: Long, docFileName: String, chunkText: String) {
        val embedding = sentenceEncoder.encodeText(chunkText)
        Log.e("APP", "Embedding dims ${embedding.size}")
        chunksDB.addChunk(
            Chunk(
                docId = docId,
                docFileName = docFileName,
                chunkData = chunkText,
                chunkEmbedding = embedding
            )
        )
    }

//    fun addChunksL(docId: Long, docFileName: String, chunks: List<TextSegment>) {
//          chunksDB.addChunksL(chunks)
//    }






//    fun getSimilarChunksL(query: String, n: Int = 3): List<Pair<Float, Chunk>> {
//       // val queryEmbedding = sentenceEncoder.encodeText(query)
//        return chunksDB.getSimilarChunksL(query, n)
//    }




    fun removeChunks(docId: Long) {
        chunksDB.removeChunks(docId)
    }

    fun getSimilarChunks(query: String, n: Int = 3): List<Pair<Float, Chunk>> {
        val queryEmbedding = sentenceEncoder.encodeText(query)
        return chunksDB.getSimilarChunks(queryEmbedding, n)
    }
}
