package com.darkfactory.plantpotting.camera

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaptureCacheCleaner
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun sweep(maxAge: Long = TimeUnit.MINUTES.toMillis(DEFAULT_MAX_AGE_MIN)) {
            val dir = File(context.cacheDir, CAPTURE_DIR)
            if (!dir.exists()) return
            val cutoff = System.currentTimeMillis() - maxAge
            dir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoff) {
                    file.delete()
                }
            }
        }

        fun captureDir(): File {
            val dir = File(context.cacheDir, CAPTURE_DIR)
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

        companion object {
            const val CAPTURE_DIR = "captures"
            const val DEFAULT_MAX_AGE_MIN = 10L
        }
    }
