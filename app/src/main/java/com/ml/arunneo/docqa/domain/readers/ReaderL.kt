package com.ml.arunneo.docqa.domain.readers

import java.io.InputStream
import dev.langchain4j.data.document.Document

abstract class ReaderL {

    abstract fun readFromInputStream(inputStream: InputStream): ArrayList<Document?>
}
