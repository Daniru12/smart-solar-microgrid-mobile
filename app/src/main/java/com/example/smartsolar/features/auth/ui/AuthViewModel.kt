package com.example.smartsolar.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsolar.features.auth.models.AuthResponse
import com.example.smartsolar.features.auth.models.LoginRequest
import com.example.smartsolar.features.auth.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val authData: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.login(request)
                .onSuccess { authData ->
                    _authState.value = AuthState.Success(authData)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Login failed. Please check your credentials.")
                }
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
