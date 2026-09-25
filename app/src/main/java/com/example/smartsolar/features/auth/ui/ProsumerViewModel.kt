package com.example.smartsolar.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsolar.features.auth.models.ProsumerProfile
import com.example.smartsolar.features.auth.models.RegisterRequest
import com.example.smartsolar.features.auth.models.UpdateProfileRequest
import com.example.smartsolar.features.auth.repository.ProsumerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class ProfileLoaded(val profile: ProsumerProfile) : ProfileState()
    data class Success(val message: String) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class ProsumerViewModel(private val repository: ProsumerRepository) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _currentProfile = MutableStateFlow<ProsumerProfile?>(null)
    val currentProfile: StateFlow<ProsumerProfile?> = _currentProfile.asStateFlow()

    fun loadProfile(token: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val profile = repository.getProfile(token)
                if (profile != null) {
                    _currentProfile.value = profile
                    _profileState.value = ProfileState.ProfileLoaded(profile)
                } else {
                    _profileState.value = ProfileState.Error("Could not load profile.")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Error loading profile.")
            }
        }
    }

    fun updateProfile(token: String, request: UpdateProfileRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val updated = repository.updateProfile(token, request)
                if (updated != null) {
                    _currentProfile.value = updated
                    _profileState.value = ProfileState.Success("Profile updated successfully!")
                    onSuccess()
                } else {
                    _profileState.value = ProfileState.Error("Failed to update profile.")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Error updating profile.")
            }
        }
    }

    fun register(request: RegisterRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val success = repository.register(request)
                if (success) {
                    _registerState.value = RegisterState.Success
                    onSuccess()
                } else {
                    _registerState.value = RegisterState.Error("Registration failed. Please try again.")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Registration error.")
            }
        }
    }

    fun requestDeactivation(token: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val success = repository.requestDeactivation(token)
                if (success) {
                    _profileState.value = ProfileState.Success("Deactivation request submitted.")
                    onSuccess()
                } else {
                    _profileState.value = ProfileState.Error("Request failed.")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Error submitting request.")
            }
        }
    }

    fun resetRegisterState() { _registerState.value = RegisterState.Idle }
    fun resetProfileState() { _profileState.value = ProfileState.Idle }
}
