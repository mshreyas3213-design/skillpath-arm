package com.bharatai.learningcompanion

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.pytorch.executorch.extension.llm.LlmCallback
import org.pytorch.executorch.extension.llm.LlmModule
import java.io.File

/**
 * On-device Llama via ExecuTorch.
 * Model files are NOT in Git. Put them in the app's private files folder:
 *   <filesDir>/llama.pte
 *   <filesDir>/tokenizer.model
 * If they are missing or loading fails, every function returns a clearly
 * labelled fallback message instead of crashing or faking model output.
 */
class ExecuTorchAIEngine(private val context: Context) : AIEngine {

    private val tag = "ExecuTorchAIEngine"
    private val lock = Mutex()
    private var llm: LlmModule? = null
    private var loadError: String? = null

    companion object {
        private const val MODEL_FILE = "llama.pte"
        private const val TOKENIZER_FILE = "tokenizer.model"
        private const val TEMPERATURE = 0.55f      // 0.5 to 0.6
        private const val MAX_TOKENS = 512         // keep within the 1024 context cap
    }

    private fun ensureLoaded(): Boolean {
        if (llm != null) return true
        if (loadError != null) return false
        val model = File(context.filesDir, MODEL_FILE)
        val tokenizer = File(context.filesDir, TOKENIZER_FILE)
        if (!model.exists() || !tokenizer.exists()) {
            loadError = "Model files not found in ${context.filesDir.absolutePath}"
            Log.w(tag, loadError!!)
            return false
        }
        return try {
            llm = LlmModule(model.absolutePath, tokenizer.absolutePath, TEMPERATURE)
            true
        } catch (e: Throwable) {
            loadError = "Model failed to load: ${e.message}"
            Log.e(tag, loadError!!, e)
            false
        }
    }

    /** Wraps text in the Llama 3 instruct chat format. */
    private fun buildPrompt(system: String, user: String): String =
        "<|begin_of_text|>" +
                "<|start_header_id|>system<|end_header_id|>\n\n$system<|eot_id|>" +
                "<|start_header_id|>user<|end_header_id|>\n\n$user<|eot_id|>" +
                "<|start_header_id|>assistant<|end_header_id|>\n\n"

    private suspend fun run(system: String, user: String): String =
        withContext(Dispatchers.Default) {
            lock.withLock {
                if (!ensureLoaded()) {
                    return@withLock "[Model not loaded] ${loadError ?: ""}"
                }
                val out = StringBuilder()
                try {
                    llm!!.generate(
                        buildPrompt(system, user),
                        MAX_TOKENS,
                        object : LlmCallback {
                            override fun onResult(result: String) { out.append(result) }
                            override fun onStats(stats: String) { Log.d(tag, stats) }
                        }
                    )
                    out.toString().replace("<|eot_id|>", "").trim()
                        .ifEmpty { "[Model returned no text]" }
                } catch (e: Throwable) {
                    Log.e(tag, "Inference failed", e)
                    "[Inference error] ${e.message}"
                }
            }
        }

    override suspend fun analyzeSkillGaps(goals: String, currentSkills: String): String =
        run(
            system = "You are a learning advisor. Be concise. Reply in plain text.",
            user = "Goal: $goals\nCurrent skills: $currentSkills\n" +
                    "List the main skill gaps in priority order, one line each."
        )

    override suspend fun generateStudyPlan(gaps: String, timeframe: String): String =
        run(
            system = "You are a study planner. Be concise. Reply in plain text.",
            user = "Skill gaps: $gaps\nTime available: $timeframe\n" +
                    "Write a short week-by-week study plan."
        )

    override suspend fun getChatResponse(userMessage: String, context: String): String =
        run(
            system = "You are a friendly offline learning companion. " +
                    "Use this learner context: $context. Do not repeat questions already asked.",
            user = userMessage
        )
}