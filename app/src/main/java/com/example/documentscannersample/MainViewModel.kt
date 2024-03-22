package com.example.documentscannersample

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private var _pdfUri = MutableStateFlow("")
    private var _imageUriList = MutableStateFlow(listOf<Uri>())

    val pdfUri: StateFlow<String> = _pdfUri.asStateFlow()
    val imageUriList = _imageUriList.asStateFlow()

    fun setPdfUri(pdfUri: String) {
        _pdfUri.value = pdfUri
    }

    fun setImageUriList(uriList: List<Uri>) {
        _imageUriList.value = uriList
    }
}