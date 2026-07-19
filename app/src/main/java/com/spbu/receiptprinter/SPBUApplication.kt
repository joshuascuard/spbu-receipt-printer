package com.spbu.receiptprinter

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.spbu.receiptprinter.data.database.DatabaseInitializer
import javax.inject.Inject

/**
 * Application class utama.
 * Hilt memerlukan @HiltAndroidApp pada Application class.
 */
@HiltAndroidApp
class SPBUApplication : Application() {

    @Inject
    lateinit var databaseInitializer: DatabaseInitializer

    override fun onCreate() {
        super.onCreate()

        // Inisialisasi data default di background thread
        CoroutineScope(Dispatchers.IO).launch {
            databaseInitializer.initialize()
        }
    }
}
