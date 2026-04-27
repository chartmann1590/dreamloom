package com.charles.app.dreamloom.llm

import android.content.Context
import java.io.File

object ModelStorage {
    fun modelFile(ctx: Context): File =
        File(ctx.filesDir, "models/${ModelConfig.FILENAME}")
}
