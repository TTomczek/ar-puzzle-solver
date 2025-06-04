package net.tomczek.ar.puzzle.solver.viewmodel

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import net.tomczek.ar.puzzle.solver.R

class ArCameraViewModel : ViewModel() {

    var statusText by mutableIntStateOf(R.string.hint_searching_puzzle)
        private set

    var currentlyProcessing by mutableStateOf(false)
        private set

    var recognitionFailures by mutableIntStateOf(0)
        private set

    var cameraPaused by mutableStateOf(false)
        private set

    fun updateStatusText(@StringRes text: Int) {
        statusText = text
    }

    fun setProcessingState(isProcessing: Boolean) {
        currentlyProcessing = isProcessing
    }

    fun incrementRecognitionFailures() {
        recognitionFailures++
    }

    fun resetRecognitionFailures() {
        recognitionFailures = 0
    }

    fun updateCameraPaused(paused: Boolean) {
        cameraPaused = paused
    }
}