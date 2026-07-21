package com.vijay.tadashi.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.vijay.tadashi.core.ai.AssistantEngine
import com.vijay.tadashi.core.ai.AIConfigurationStore
import com.vijay.tadashi.core.ai.EncryptedAIConfigurationStore
import com.vijay.tadashi.core.ai.ProviderAssistantEngine
import com.vijay.tadashi.core.logger.Logger
import com.vijay.tadashi.core.ai.repository.AIRepository
import com.vijay.tadashi.core.ai.repository.AIRepositoryImpl
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
/**
 * Application-wide dependency graph.
 */
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

    @Provides
    @Singleton
    fun provideAIConfigurationSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            "ai_configuration",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun provideAIConfigurationStore(
        sharedPreferences: SharedPreferences
    ): AIConfigurationStore = EncryptedAIConfigurationStore(sharedPreferences)

    @Provides
    @Singleton
    fun provideAIRepository(
        impl: AIRepositoryImpl
    ): AIRepository = impl

    @Provides
    @Singleton
    fun provideAssistantEngine(
        impl: ProviderAssistantEngine
    ): AssistantEngine = impl
}
