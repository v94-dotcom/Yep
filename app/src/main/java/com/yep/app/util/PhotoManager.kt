package com.yep.app.util

import android.content.Context
import java.io.File

object PhotoManager {
    fun newPhotoFile(context: Context, itemId: String): File {
        val dir = File(context.filesDir, "photos").also { it.mkdirs() }
        return File(dir, "${itemId}_${System.currentTimeMillis()}.jpg")
    }

    fun cleanupOldPhotos(context: Context) {
        val dir = File(context.filesDir, "photos")
        if (!dir.exists()) return
        val cutoff = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        dir.listFiles()?.forEach { if (it.lastModified() < cutoff) it.delete() }
    }
}
