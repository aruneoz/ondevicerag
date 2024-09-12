package com.ml.arunneo.docqa.domain

/*
@Time    : 2024/09/01 17:45
@Author  : asanthan
@File    : DocumentsUseCase.kt
@Descriptor: This is the Ondevice LLM Demonstration with Gemma and Media Pipe
*/

import android.util.Log
import com.ml.arunneo.docqa.data.Document
import com.ml.arunneo.docqa.data.DocumentsDB
import com.ml.arunneo.docqa.domain.readers.Readers
import com.ml.arunneo.docqa.domain.splitters.WhiteSpaceSplitter
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import setProgressDialogText
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import dev.langchain4j.data.document.Document as LangchainDocument


@Singleton
class DocumentsUseCase
@Inject
constructor(private val chunksUseCase: ChunksUseCase, private val documentsDB: DocumentsDB) {

    suspend fun addDocument(
        inputStream: InputStream,
        fileName: String,
        documentType: Readers.DocumentType
    ) =
        withContext(Dispatchers.IO) {
            val text =
                Readers.getReaderForDocType(documentType).readFromInputStream(inputStream)
                    ?: return@withContext
            Log.e("APP", "PDF Text: $text")
            val newDocId =
                documentsDB.addDocument(
                    Document(
                        docText = text,
                        docFileName = fileName,
                        docAddedTime = System.currentTimeMillis()
                    )
                )
            setProgressDialogText("Creating chunks...")
            val chunks = WhiteSpaceSplitter.createChunks(text, chunkSize = 500, chunkOverlap = 50)
            setProgressDialogText("Adding chunks to database...")
            val size = chunks.size
            chunks.forEachIndexed { index, s ->
                setProgressDialogText("Added ${index+1}/${size} chunk(s) to database...")
                chunksUseCase.addChunk(newDocId, fileName, s)
            }
        }

    suspend fun addDocumentLangChain(
        inputStream: InputStream,
        fileName: String,
        documentType: Readers.DocumentType
    ) =
        withContext(Dispatchers.IO) {
            val docs =
                Readers.getReaderForDocTypeL(documentType).readFromInputStream(inputStream)
                    ?: return@withContext
            Log.e("APP", "Retrieved Langchain Text: ${docs[0]!!.toString()}")
            if(docs.size>1) {
                val newDocId =
                    documentsDB.addDocument(
                        Document(
                            docText = docs[0]!!.text(),
                            docFileName = fileName,
                            docAddedTime = System.currentTimeMillis()
                        )
                    )
                setProgressDialogText("Creating chunks...")
                // val chunks = WhiteSpaceSplitter.createChunks(text, chunkSize = 500, chunkOverlap = 50)


                // val document: LangchainDocument = loadDocument(toPath("cassandra.pdf"), ApachePdfBoxDocumentParser());
                setProgressDialogText("Adding chunks to database...")
                docs.forEachIndexed { index, doc ->
                    val chunks: List<TextSegment> = DocumentSplitters
                        .recursive(500, 50)
                        .split(doc)
                    val size = chunks.size
                    setProgressDialogText("Going to add ${size / 500 } chunk(s) to database...")
                   // chunksUseCase.addChunk(newDocId, fileName, s.text())
                    chunks.forEachIndexed { index, s ->
                        setProgressDialogText("Added ${index + 1}/${size} chunk(s) to database...")
                        chunksUseCase.addChunk(newDocId, fileName, s.text())
                        //chunksUseCase.addChunksL(newDocId, fileName,chunks)
                    }
                }
            }
        }


    fun getAllDocuments(): Flow<List<Document>> {
        return documentsDB.getAllDocuments()
    }

    fun removeDocument(docId: Long) {
        documentsDB.removeDocument(docId)
        chunksUseCase.removeChunks(docId)
    }

    fun getDocsCount(): Long {
        return documentsDB.getDocsCount()
    }



}
