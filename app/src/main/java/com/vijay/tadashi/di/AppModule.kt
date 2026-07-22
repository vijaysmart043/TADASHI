package com.vijay.tadashi.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.vijay.tadashi.core.ai.AssistantEngine
import com.vijay.tadashi.core.ai.AIConfigurationStore
import com.vijay.tadashi.core.ai.EncryptedAIConfigurationStore
import com.vijay.tadashi.core.ai.ProviderAssistantEngine
import com.vijay.tadashi.core.ai.gemini.network.GeminiApi
import com.vijay.tadashi.core.ai.gemini.network.GeminiService
import com.vijay.tadashi.core.logger.Logger
import com.vijay.tadashi.core.ai.repository.AIRepository
import com.vijay.tadashi.core.ai.repository.AIRepositoryImpl
import com.vijay.tadashi.core.ai.streaming.SimulatedTextChunkStreamer
import com.vijay.tadashi.core.ai.streaming.TextChunkStreamer
import com.vijay.tadashi.core.voice.SpeechRecognizerManager
import com.vijay.tadashi.core.voice.TextToSpeechManager
import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
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
    fun provideKotlinxJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    @Provides
    @Singleton
    fun provideTextChunkStreamer(
        impl: SimulatedTextChunkStreamer
    ): TextChunkStreamer = impl

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val safeUrl = request.url.newBuilder().query(null).build()
                val startMs = System.currentTimeMillis()
                Log.d("TADASHI-GEMINI", "HTTP ${request.method} $safeUrl")
                try {
                    val response: Response = chain.proceed(request)
                    val elapsedMs = System.currentTimeMillis() - startMs
                    Log.d("TADASHI-GEMINI", "HTTP ${response.code} (${elapsedMs}ms) $safeUrl")
                    response
                } catch (e: Exception) {
                    val elapsedMs = System.currentTimeMillis() - startMs
                    Log.d("TADASHI-GEMINI", "HTTP failed (${elapsedMs}ms) $safeUrl", e)
                    throw e
                }
            }
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideGeminiApi(
        retrofit: Retrofit
    ): GeminiApi {
        return retrofit.create(GeminiApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGeminiService(
        api: GeminiApi,
        json: Json,
        mapper: com.vijay.tadashi.core.ai.gemini.network.GeminiMapper,
        textChunkStreamer: com.vijay.tadashi.core.ai.streaming.TextChunkStreamer
    ): GeminiService {
        return GeminiService(
            api = api,
            json = json,
            mapper = mapper,
            textChunkStreamer = textChunkStreamer
        )
    }

    @Provides
    @Singleton
    fun provideAssistantEngine(
        impl: ProviderAssistantEngine
    ): AssistantEngine = impl
}
