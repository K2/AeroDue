package com.aerodue.llm

import android.content.Context
import com.aerodue.core.inference.ModelProfiles
import java.io.File

object LlmModelPaths {

    fun modelsDirectory(context: Context): File =
        File(context.filesDir, "models").apply { mkdirs() }

    fun defaultModelFile(context: Context): File =
        File(modelsDirectory(context), ModelProfiles.defaultLitertLmFilename)

    fun hasDefaultModel(context: Context): Boolean =
        defaultModelFile(context).isFile && defaultModelFile(context).length() > 0L
}
