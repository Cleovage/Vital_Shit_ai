package com.example.vitaai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

@HiltAndroidApp
class VitaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("VitaAppCrash", "FATAL EXCEPTION in thread ${thread.name}", throwable)
                val crashFile = File(cacheDir, "crash.txt")
                FileWriter(crashFile).use { fw ->
                    PrintWriter(fw).use { pw ->
                        pw.println("Thread: ${thread.name}")
                        throwable.printStackTrace(pw)
                    }
                }
                Log.e("VitaAppCrash", "Wrote crash log to ${crashFile.absolutePath}")
            } catch (e: Throwable) {
                Log.e("VitaAppCrash", "Failed to write crash log to file", e)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}