package com.example.vitaai.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth? {
        return try {
            Firebase.auth
        } catch (t: Throwable) {
            android.util.Log.e("FirebaseModule", "Failed to initialize FirebaseAuth", t)
            null
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore? {
        return try {
            Firebase.firestore
        } catch (t: Throwable) {
            android.util.Log.e("FirebaseModule", "Failed to initialize FirebaseFirestore", t)
            null
        }
    }
}
