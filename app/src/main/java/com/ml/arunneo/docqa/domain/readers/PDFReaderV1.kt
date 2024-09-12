package com.ml.arunneo.docqa.domain.readers

import android.util.Log
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.Metadata
import java.io.InputStream

class PDFReaderV1 : ReaderL() {

    override fun readFromInputStream(inputStream: InputStream): ArrayList<Document?> {
        val pdfReader = PdfReader(inputStream)
        val metadata = Metadata()
        //val docs = Array(20) { LangchainDocument() }

        val docs : ArrayList<Document?> = ArrayList()
        val it: Iterator<*> = pdfReader.info.iterator()
        for (i in 1..pdfReader.numberOfPages) {


                while (it.hasNext()) {
                    val pair = it.next() as Map.Entry<*, *>
                    // println(pair.key.toString() + " = " + pair.value)
                    metadata.put(
                        pair.key.toString(),
                        pair.value.toString()
                    )// avoids a ConcurrentModificationException
                }


            val extractedText = PdfTextExtractor.getTextFromPage(pdfReader, i)
            if (extractedText != null && extractedText.isNotBlank()) {
                docs.add(Document(PdfTextExtractor.getTextFromPage(pdfReader, i), metadata))
                Log.e("APP", "PDF Text: ${PdfTextExtractor.getTextFromPage(pdfReader, i)}")
            }



                //pdfText += "\n" + PdfTextExtractor.getTextFromPage(pdfReader, i)
            }

        return docs
    }
}
