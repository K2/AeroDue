package com.aerodue.llm

import android.content.Context
import com.aerodue.core.inference.ModelProfiles
import java.io.File

/**
 * Copies bundled `.litertlm` from assets to [Context.filesDir] on first run.
 * Large models are gitignored; CI/dev runs [scripts/download-models.sh] before build.
 */
object ModelInstaller {

    private const val ASSET_DIR = "models"
    private const val INSTALLED_MARKER = ".installed"

    fun ensureDefaultModelFromAssets(context: Context) {
        val destDir = LlmModelPaths.modelsDirectory(context)
        val marker = File(destDir, INSTALLED_MARKER)
        val dest = LlmModelPaths.defaultModelFile(context)
        if (dest.isFile && dest.length() > 0L) return

        val assetName = ModelProfiles.defaultLitertLmFilename
        try {
            context.assets.open("$ASSET_DIR/$assetName").use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            marker.writeText(assetName)
        } catch (_: Exception) {
            // Model not bundled — RuleOnlyRationaleRunner will be used
        }
    }
}
