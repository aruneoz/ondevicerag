package com.ml.arunneo.docqa

import android.app.Application
import com.ml.arunneo.docqa.data.ObjectBoxStore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DocQAApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ObjectBoxStore.init(this)
    }
}
