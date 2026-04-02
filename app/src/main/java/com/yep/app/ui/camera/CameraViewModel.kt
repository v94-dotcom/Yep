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

    enum class State { IDLE, CAPTURING, DONE }

    private val _state = MutableStateFlow(State.IDLE)
    val state: StateFlow<State> = _state.asStateFlow()

    fun captureAndConfirm(
        context: Context,
        imageCapture: ImageCapture,
        itemId: String,
        executor: Executor
    ) {
        if (_state.value != State.IDLE) return
        _state.value = State.CAPTURING

        val file = PhotoManager.newPhotoFile(context, itemId)
        val opts = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(opts, executor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                viewModelScope.launch {
                    repository.insertConfirmation(
                        Confirmation(
                            itemId = itemId,
                            date = DateUtils.today(),
                            confirmedAt = System.currentTimeMillis(),
                            photoPath = file.absolutePath
                        )
                    )
                    _state.value = State.DONE
                }
            }

            override fun onError(e: ImageCaptureException) {
                _state.value = State.IDLE
            }
        })
    }

    fun skipPhoto(itemId: String) {
        viewModelScope.launch {
            repository.insertConfirmation(
                Confirmation(
                    itemId = itemId,
                    date = DateUtils.today(),
                    confirmedAt = System.currentTimeMillis(),
                    photoPath = null
                )
            )
            _state.value = State.DONE
        }
    }
}
