package com.fake.autotaskapp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

object AppEvent {
    val textState = MutableStateFlow("")

    fun updateText(text: String) {
        textState.update { text }
    }
}
