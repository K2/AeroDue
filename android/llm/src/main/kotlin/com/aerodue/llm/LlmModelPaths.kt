package com.aerodue.llm

import android.content.Context
import com.aerodue.core.inference.ModelProfiles
import java.io.File

object LlmModelPaths {

    fun modelsDirectory(context: Context): File =
        File(context.filesDir, "models").apply { mkdirs() }

    /** External app files dir (adb-pushable without root): Android/data/<pkg>/files/models. */
    fun externalModelsDirectory(context: Context): File? =
        context.getExternalFilesDir("models")

    private fun internalModelFile(context: Context): File =
        File(modelsDirectory(context), ModelProfiles.defaultLitertLmFilename)

    private fun externalModelFile(context: Context): File? =
        externalModelsDirectory(context)?.let { File(it, ModelProfiles.defaultLitertLmFilename) }

    /**
     * Resolve the model: prefer internal storage, then fall back to the external app files dir
     * (handy for large models pushed via `adb push` during development).
     */
    fun defaultModelFile(context: Context): File {
        val internal = internalModelFile(context)
        if (internal.isFile && internal.length() > 0L) return internal
        val external = externalModelFile(context)
        if (external != null && external.isFile && external.length() > 0L) return external
        return internal
    }

    fun hasDefaultModel(context: Context): Boolean =
        defaultModelFile(context).let { it.isFile && it.length() > 0L }
}
