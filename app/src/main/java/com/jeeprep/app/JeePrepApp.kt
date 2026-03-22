package com.jeeprep.app

import android.app.Application
import com.jeeprep.app.data.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class JeePrepApp : Application() {

    @Inject
    lateinit var databaseInitializer: DatabaseInitializer

    override fun onCreate() {
        super.onCreate()
        databaseInitializer.initialize()
    }
}
