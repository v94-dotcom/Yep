package com.yep.app.ui.camera

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yep.app.data.entities.Confirmation
import com.yep.app.data.repository.YepRepository
import com.yep.app.util.DateUtils
import com.yep.app.util.PhotoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: YepRepository
) : ViewModel() {

    enum class State { IDLE, CAPTURING, READY, DONE }

    private val _state = MutableStateFlow(State.IDLE)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _capturedPaths = MutableStateFlow<List<String>>(emptyList())
    val capturedPaths: StateFlow<List<String>> = _capturedPaths.asStateFlow()

    fun capturePhoto(
        context: Context,
        imageCapture: ImageCapture,
        itemId: String,
        executor: Executor
    ) {
        if (_state.value != State.IDLE && _state.value != State.READY) return
        _state.value = State.CAPTURING

        val file = PhotoManager.newPhotoFile(context, itemId)
        val opts = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(opts, executor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                _capturedPaths.value = _capturedPaths.value + file.absolutePath
                _state.value = State.READY
            }

            override fun onError(e: ImageCaptureException) {
                _state.value = if (_capturedPaths.value.isEmpty()) State.IDLE else State.READY
            }
        })
    }

    fun finishWithPhotos(itemId: String) {
        viewModelScope.launch {
            repository.insertConfirmation(
                Confirmation(
                    itemId = itemId,
                    date = DateUtils.today(),
                    confirmedAt = System.currentTimeMillis(),
                    photoPaths = _capturedPaths.value.ifEmpty { null }
                )
            )
            _state.value = State.DONE
        }
    }

    fun skipPhoto(itemId: String) {
        viewModelScope.launch {
            repository.insertConfirmation(
                Confirmation(
                    itemId = itemId,
                    date = DateUtils.today(),
                    confirmedAt = System.currentTimeMillis(),
                    photoPaths = null
                )
            )
            _state.value = State.DONE
        }
    }
}
