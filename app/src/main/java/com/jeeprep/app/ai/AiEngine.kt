package com.jeeprep.app.ai

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var engine: Engine? = null
    private var isInitialized = false
    private val initMutex = Mutex()
    private val sessionMutex = Mutex()
    private var activeConversation: Conversation? = null

    val isReady: Boolean get() = isInitialized
    val isBusy: Boolean get() = sessionMutex.isLocked

    suspend fun initialize() = withContext(Dispatchers.IO) {
        initMutex.withLock {
            if (isInitialized) return@withContext

            val modelFile = ModelDownloadManager.getModelFile(context)
            if (!modelFile.exists()) {
                throw IllegalStateException("Model file not found. Download it first.")
            }

            val config = EngineConfig(
                modelPath = modelFile.absolutePath,
                backend = try {
                    Backend.GPU()
                } catch (_: Exception) {
                    Backend.CPU()
                },
                cacheDir = context.cacheDir.path
            )

            engine = Engine(config)
            engine!!.initialize()
            isInitialized = true
        }
    }

    private suspend fun ensureInitialized() {
        if (!isInitialized && ModelDownloadManager.isModelDownloaded(context)) {
            initialize()
        }
    }

    fun generateResponse(
        prompt: String,
        systemInstruction: String = PromptTemplates.DEFAULT_SYSTEM,
        temperature: Double = 0.7,
        topK: Int = 40,
        topP: Double = 0.95
    ): Flow<String> = flow {
        ensureInitialized()
        val eng = engine
        if (eng == null) {
            emit("[AI model not available. Download it from the Model Download screen.]")
            return@flow
        }

        sessionMutex.withLock {
            // Close any leftover session
            try { activeConversation?.close() } catch (_: Exception) {}
            activeConversation = null

            val conversationConfig = ConversationConfig(
                systemInstruction = Contents.of(systemInstruction),
                samplerConfig = SamplerConfig(
                    topK = topK,
                    topP = topP,
                    temperature = temperature
                )
            )

            val conversation = eng.createConversation(conversationConfig)
            activeConversation = conversation
            try {
                conversation.sendMessageAsync(prompt)
                    .collect { message ->
                        emit(message.toString())
                    }
            } finally {
                try { conversation.close() } catch (_: Exception) {}
                activeConversation = null
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun generateResponseSync(
        prompt: String,
        systemInstruction: String = PromptTemplates.DEFAULT_SYSTEM,
        temperature: Double = 0.7
    ): String = withContext(Dispatchers.IO) {
        ensureInitialized()
        val eng = engine
            ?: return@withContext "[AI model not available. Download it from the Model Download screen.]"

        sessionMutex.withLock {
            try { activeConversation?.close() } catch (_: Exception) {}
            activeConversation = null

            val conversationConfig = ConversationConfig(
                systemInstruction = Contents.of(systemInstruction),
                samplerConfig = SamplerConfig(
                    topK = 40,
                    topP = 0.95,
                    temperature = temperature
                )
            )

            val conversation = eng.createConversation(conversationConfig)
            activeConversation = conversation
            try {
                conversation.sendMessage(prompt).toString()
            } finally {
                try { conversation.close() } catch (_: Exception) {}
                activeConversation = null
            }
        }
    }

    fun shutdown() {
        engine?.close()
        engine = null
        isInitialized = false
    }
}
