package com.example.builder

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.ProjectFileEntity
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipExporter {

    fun exportProjectToZip(
        context: Context,
        projectName: String,
        files: List<ProjectFileEntity>
    ): File? {
        return try {
            val cleanName = projectName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
            val zipDir = File(context.cacheDir, "exports")
            if (!zipDir.exists()) zipDir.mkdirs()

            val zipFile = File(zipDir, "${cleanName}_project.zip")
            if (zipFile.exists()) zipFile.delete()

            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                for (file in files) {
                    val entryName = file.path.removePrefix("/")
                    zos.putNextEntry(ZipEntry(entryName))
                    zos.write(file.content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }
            zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String = "application/zip") {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Export ${file.name}"))
        } catch (e: Exception) {
            // fallback plain share or toast
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Exported file: ${file.name} (Location: ${file.absolutePath})")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(sendIntent)
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            shareFile(context, apkFile, "application/vnd.android.package-archive")
        }
    }
}
