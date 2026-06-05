package com.aerodue.core.inference

/**
 * Mirrors backend/aerodue/inference/models.yaml — Kotlin is canonical on device.
 */
object ModelProfiles {
    const val defaultProfileId = "qwen2_5_0_5b_litertlm"

    /** On-device LiteRT-LM bundle (see scripts/download-models.sh). */
    const val defaultLitertLmFilename = "qwen2.5-0.5b-instruct.litertlm"

    data class Profile(
        val id: String,
        val repo: String,
        val filename: String,
        val format: String,
        val ramBudgetMb: Int,
    )

    val profiles = listOf(
        Profile(
            id = "qwen2_5_0_5b_litertlm",
            repo = "litert-community/Qwen2.5-0.5B-Instruct",
            filename = defaultLitertLmFilename,
            format = "litertlm",
            ramBudgetMb = 1200,
        ),
        Profile(
            id = "smollm2_360m_q4",
            repo = "HuggingFaceTB/SmolLM2-360M-Instruct-GGUF",
            filename = "smollm2-360m-instruct-q4_k_m.gguf",
            format = "gguf",
            ramBudgetMb = 600,
        ),
    )
}
