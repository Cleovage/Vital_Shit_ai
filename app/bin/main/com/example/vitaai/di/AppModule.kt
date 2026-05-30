package com.example.vitaai.di

import android.content.Context
import androidx.room.Room
import com.example.vitaai.data.local.VitaDao
import com.example.vitaai.data.local.VitaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VitaDatabase {
        return Room.databaseBuilder(context, VitaDatabase::class.java, "vita_ai.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideVitaDao(database: VitaDatabase): VitaDao = database.vitaDao()
}
