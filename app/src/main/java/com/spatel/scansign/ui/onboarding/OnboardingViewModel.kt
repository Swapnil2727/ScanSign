package com.spatel.scansign.ui.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spatel.scansign.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val preferencesDataSource: UserPreferencesDataSource,
) : ViewModel() {

    var nameInput by mutableStateOf("")
        private set

    val isNameValid: Boolean get() = nameInput.trim().length >= 5

    fun onNameChange(value: String) {
        nameInput = value
    }

    fun saveAndProceed(onDone: () -> Unit) {
        if (!isNameValid) return
        viewModelScope.launch {
            preferencesDataSource.setUserName(nameInput.trim())
            preferencesDataSource.setOnboardingComplete()
            onDone()
        }
    }
}
