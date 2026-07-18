package com.vijay.tadashi.di

import android.content.Context
import com.vijay.tadashi.core.logger.Logger
import com.vijay.tadashi.core.voice.SpeechRecognizerManager
import com.vijay.tadashi.core.voice.TextToSpeechManager
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
    fun provideLogger(): Logger = Logger

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideSpeechRecognizerManager(@ApplicationContext context: Context): SpeechRecognizerManager =
        SpeechRecognizerManager(context)

    @Provides
    @Singleton
    fun provideTextToSpeechManager(@ApplicationContext context: Context): TextToSpeechManager =
        TextToSpeechManager(context)
}