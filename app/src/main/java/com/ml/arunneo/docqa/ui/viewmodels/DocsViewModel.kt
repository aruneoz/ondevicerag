package com.ml.arunneo.docqa.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.ml.arunneo.docqa.domain.DocumentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DocsViewModel @Inject constructor(val documentsUseCase: DocumentsUseCase) : ViewModel() {

    val documentsFlow = documentsUseCase.getAllDocuments()
}
