package com.nutrition.express.util

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.facebook.common.memory.PooledByteBuffer
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSources
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.nutrition.express.application.Constant
import com.nutrition.express.application.TumbApp
import com.nutrition.express.util.FileUtils.imageSaved
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileOutputStream


fun setTumblrAvatarUri(view: SimpleDraweeView, name: String, size: Int) {
    var url = Constant.BASE_URL + "/v2/blog/$name/avatar/"
    if (size in 1..512) {
        url += size
    }
    val imageRequest = ImageRequestBuilder
        .newBuilderWithSource(Uri.parse(url))
        .setCacheChoice(ImageRequest.CacheChoice.SMALL)
        .build()
    val controller: DraweeController = Fresco.newDraweeControllerBuilder()
        .setOldController(view.controller)
        .setImageRequest(imageRequest)
        .build()
    view.controller = controller
}

fun save(uri: Uri): LiveData<Uri> {
    val request = ImageRequest.fromUri(uri)
    val pipeline = Fresco.getImagePipeline()
    val dataSource = pipeline.fetchEncodedImage(request, null)
    return liveData(Dispatchers.IO, 30000) {
        try {
            val result: CloseableReference<PooledByteBuffer>? =
                DataSources.waitForFinalResult(dataSource)
            if (result != null) {
                saveToFile(result.get(), FileUtils.createImageFile(uri))
                emit(uri)
            } else {
                emit(Uri.EMPTY)
            }
        } catch (throwable: Throwable) {
            emit(Uri.EMPTY)
        } finally {
            dataSource.close()
        }
    }
}


fun saveAll(uris: List<Uri>): LiveData<Uri> {
    return liveData(Dispatchers.IO, 60000) {
        for (uri in uris) {
            if (imageSaved(uri)) {
                emit(uri)
                continue
            }
            val request = ImageRequest.fromUri(uri)
            val pipeline = Fresco.getImagePipeline()
            val dataSource = pipeline.fetchEncodedImage(request, null)
            try {
                val result: CloseableReference<PooledByteBuffer>? =
                    DataSources.waitForFinalResult(dataSource)
                if (result != null) {
                    saveToFile(result.get(), FileUtils.createImageFile(uri))
                    emit(uri)
                } else {
                    emit(Uri.EMPTY)
                }
            } catch (throwable: Throwable) {
                emit(Uri.EMPTY)
            } finally {
                dataSource.close()
            }
        }
    }
}

private fun saveToFile(buffer: PooledByteBuffer, file: File) {
    if (!file.exists() || file.length() != buffer.size().toLong()) {
        FileOutputStream(file).use {
            val bytes = ByteArray(buffer.size())
            buffer.read(0, bytes, 0, buffer.size())
            it.write(bytes)
            it.flush()
            galleryAddPic(file)
        }
    }
}

private fun galleryAddPic(f: File) {
    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    val contentUri = Uri.fromFile(f)
    mediaScanIntent.data = contentUri
    TumbApp.app.sendBroadcast(mediaScanIntent)
}

