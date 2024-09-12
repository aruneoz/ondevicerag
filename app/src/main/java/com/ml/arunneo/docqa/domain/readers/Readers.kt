package com.ml.arunneo.docqa.domain.readers

class Readers {

    enum class DocumentType {
        PDF,
        MS_DOCX
    }

    companion object {

        fun getReaderForDocType(docType: DocumentType): Reader {
            return when (docType) {
                DocumentType.PDF -> PDFReader()
                DocumentType.MS_DOCX -> DOCXReader()
            }
        }

        fun getReaderForDocTypeL(docType: DocumentType): ReaderL {
            return  PDFReaderV1()


        }
    }
}
