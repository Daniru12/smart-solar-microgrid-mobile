package com.example.smartsolar.features.backoffice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsolar.features.backoffice.models.BackofficeProsumer
import com.example.smartsolar.features.backoffice.models.BackofficeUser
import com.example.smartsolar.features.backoffice.repository.BackofficeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackofficeViewModel(private val repository: BackofficeRepository) : ViewModel() {

    private val _users = MutableStateFlow<List<BackofficeUser>>(emptyList())
    val users: StateFlow<List<BackofficeUser>> = _users.asStateFlow()

    private val _prosumers = MutableStateFlow<List<BackofficeProsumer>>(emptyList())
    val prosumers: StateFlow<List<BackofficeProsumer>> = _prosumers.asStateFlow()

    private val _deactivationRequests = MutableStateFlow<List<BackofficeProsumer>>(emptyList())
    val deactivationRequests: StateFlow<List<BackofficeProsumer>> = _deactivationRequests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadAll(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            loadUsers(token)
            loadProsumers(token)
            loadDeactivationRequests(token)
            _isLoading.value = false
        }
    }

    fun loadUsers(token: String) {
        viewModelScope.launch {
            repository.getUsers(token).onSuccess {
                _users.value = it
            }
        }
    }

    fun loadProsumers(token: String) {
        viewModelScope.launch {
            repository.getProsumers(token).onSuccess {
                _prosumers.value = it
            }
        }
    }

    fun loadDeactivationRequests(token: String) {
        viewModelScope.launch {
            repository.getDeactivationRequests(token).onSuccess {
                _deactivationRequests.value = it
            }
        }
    }

    fun toggleUserStatus(token: String, user: BackofficeUser) {
        val newStatus = if (user.status.equals("Active", ignoreCase = true)) "Inactive" else "Active"
        viewModelScope.launch {
            repository.updateUserStatus(token, user.id, newStatus).onSuccess {
                _message.value = "User ${user.email} updated to $newStatus"
                loadUsers(token)
            }.onFailure {
                _message.value = it.message ?: "Failed to update user status"
            }
        }
    }

    fun approveDeactivation(token: String, prosumerId: String) {
        viewModelScope.launch {
            repository.approveDeactivation(token, prosumerId).onSuccess {
                _message.value = "Deactivation request approved"
                loadDeactivationRequests(token)
                loadProsumers(token)
            }.onFailure {
                _message.value = it.message ?: "Failed to approve deactivation"
            }
        }
    }

    fun rejectDeactivation(token: String, prosumerId: String) {
        viewModelScope.launch {
            repository.rejectDeactivation(token, prosumerId).onSuccess {
                _message.value = "Deactivation request rejected"
                loadDeactivationRequests(token)
                loadProsumers(token)
            }.onFailure {
                _message.value = it.message ?: "Failed to reject deactivation"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
