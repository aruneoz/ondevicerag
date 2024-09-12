package com.ml.arunneo.docqa.data

//import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel
import android.util.Log
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel
import dev.langchain4j.model.embedding.onnx.PoolingMode
import dev.langchain4j.store.embedding.EmbeddingMatch
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore



class ChunksDB {

    private val chunksBox = ObjectBoxStore.store.boxFor(Chunk::class.java)
    //private val embeddingModel = AllMiniLmL6V2EmbeddingModel()
//    var embeddingModel: EmbeddingModel? = OnnxEmbeddingModel(
//        "/data/local/tmp/llm/all-MiniLM-L6-V2.onnx",
//        "/data/local/tmp/llm/tokenizer.json",
//        PoolingMode.MEAN
//    )
    private val embeddingStore: EmbeddingStore<TextSegment> = InMemoryEmbeddingStore()

    fun addChunk(chunk: Chunk) {
        chunksBox.put(chunk)
    }

//        fun addChunksL(chunks: List<TextSegment>) {
//        val embeddings: List<Embedding> = embeddingModel!!.embedAll(chunks).content()
//        embeddingStore.addAll(embeddings, chunks);
//
//    }

//    fun getSimilarChunksL(query: String, n: Int = 3): List<Pair<Float, Chunk>> {
//        /*
//        Use maxResultCount to set the maximum number of objects to return by the ANN condition.
//        Hint: it can also be used as the "ef" HNSW parameter to increase the search quality in combination
//        with a query limit. For example, use maxResultCount of 100 with a Query limit of 10 to have 10 results
//        that are of potentially better quality than just passing in 10 for maxResultCount
//        (quality/performance tradeoff).
//         */
//
//////        val contentRetriever: ContentRetriever = EmbeddingStoreContentRetriever.builder()
//////            .embeddingStore(embeddingStore)
//////            .embeddingModel(embeddingModel)
//////            .maxResults(2) // on each interaction we will retrieve the 2 most relevant segments
//////            .minScore(0.5) // we want to retrieve segments at least somewhat similar to user query
//////            .build()
//////
//////        contentRetriever.retrieve(Query())
////
//        val queryEmbedding = embeddingModel!!.embed(query).content()
//        //val relevant = embeddingStore.findRelevant(queryEmbedding, 1)
//        val embeddingsearchRequest = EmbeddingSearchRequest(queryEmbedding, n, 0.5, null)
//        val relevant = embeddingStore.search(embeddingsearchRequest)
//        val embeddingMatch: List<EmbeddingMatch<TextSegment>> = relevant.matches()
//        val information = embeddingMatch[0].embedded().text()
//        val score = embeddingMatch[0].score()
//        Log.e("APP", "Matching Results: ${information} + score: ${score}")
//        val chunks = mutableListOf<Pair<Float, Chunk>>()
//        embeddingMatch.forEach{
//            val chunk = Chunk(it.embeddingId().toLong(),it.embeddingId().toLong(),"",it.embedded().text(),it.embedding().vector())
//            chunks.add(Pair(it.score().toFloat(), chunk))
//        }
//
//        return chunks
//    }

    fun getSimilarChunks(queryEmbedding: FloatArray, n: Int = 3): List<Pair<Float, Chunk>> {
        /*
        Use maxResultCount to set the maximum number of objects to return by the ANN condition.
        Hint: it can also be used as the "ef" HNSW parameter to increase the search quality in combination
        with a query limit. For example, use maxResultCount of 100 with a Query limit of 10 to have 10 results
        that are of potentially better quality than just passing in 10 for maxResultCount
        (quality/performance tradeoff).
         */
        return chunksBox
            .query(Chunk_.chunkEmbedding.nearestNeighbors(queryEmbedding, 25))
            .build()
            .findWithScores()
            .map { Pair(it.score.toFloat(), it.get()) }
            .subList(0, n)
    }

    fun removeChunks(docId: Long) {
        chunksBox.removeByIds(chunksBox.query(Chunk_.docId.equal(docId)).build().findIds().toList())
    }


}
