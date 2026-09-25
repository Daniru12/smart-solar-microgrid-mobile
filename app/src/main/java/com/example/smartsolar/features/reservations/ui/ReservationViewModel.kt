package com.example.smartsolar.features.reservations.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsolar.features.reservations.models.CreateReservationRequest
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.features.reservations.models.UpdateReservationRequest
import com.example.smartsolar.features.reservations.network.DashboardSummary
import com.example.smartsolar.features.reservations.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReservationUiState {
    object Idle : ReservationUiState()
    object Loading : ReservationUiState()
    data class Success(val message: String = "") : ReservationUiState()
    data class Error(val message: String) : ReservationUiState()
}

class ReservationViewModel(private val repository: ReservationRepository) : ViewModel() {

    private val _myReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val myReservations: StateFlow<List<Reservation>> = _myReservations.asStateFlow()

    private val _allReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val allReservations: StateFlow<List<Reservation>> = _allReservations.asStateFlow()

    private val _pendingReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val pendingReservations: StateFlow<List<Reservation>> = _pendingReservations.asStateFlow()

    private val _selectedReservation = MutableStateFlow<Reservation?>(null)
    val selectedReservation: StateFlow<Reservation?> = _selectedReservation.asStateFlow()

    private val _dashboardSummary = MutableStateFlow<DashboardSummary?>(null)
    val dashboardSummary: StateFlow<DashboardSummary?> = _dashboardSummary.asStateFlow()

    private val _uiState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadMyReservations(token: String, nic: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try { _myReservations.value = repository.getByNic(token, nic) }
            catch (e: Exception) { /* silently keep previous list */ }
            finally { _isLoading.value = false }
        }
    }

    fun searchMyReservations(token: String, nic: String, status: String?, from: String?, to: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try { _myReservations.value = repository.searchByProsumer(token, nic, status, from, to) }
            catch (e: Exception) { _uiState.value = ReservationUiState.Error(e.message ?: "Search failed") }
            finally { _isLoading.value = false }
        }
    }

    fun loadById(token: String, id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try { _selectedReservation.value = repository.getById(token, id) }
            catch (e: Exception) { _uiState.value = ReservationUiState.Error(e.message ?: "Load failed") }
            finally { _isLoading.value = false }
        }
    }

    fun loadAll(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try { _allReservations.value = repository.getAll(token) }
            catch (e: Exception) { /* keep previous */ }
            finally { _isLoading.value = false }
        }
    }

    fun loadPending(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try { _pendingReservations.value = repository.getPending(token) }
            catch (e: Exception) { /* keep previous */ }
            finally { _isLoading.value = false }
        }
    }

    fun loadDashboardSummary(token: String) {
        viewModelScope.launch {
            try { _dashboardSummary.value = repository.getDashboardSummary(token) }
            catch (e: Exception) { /* ignore dashboard load errors */ }
        }
    }

    fun createReservation(token: String, request: CreateReservationRequest, onSuccess: (Reservation) -> Unit) {
        viewModelScope.launch {
            _uiState.value = ReservationUiState.Loading
            try {
                val result = repository.create(token, request)
                if (result != null) {
                    _uiState.value = ReservationUiState.Success("Reservation created successfully!")
                    onSuccess(result)
                } else {
                    _uiState.value = ReservationUiState.Error("Failed to create reservation.")
                }
            } catch (e: Exception) {
                _uiState.value = ReservationUiState.Error(e.message ?: "An error occurred.")
            }
        }
    }

    fun updateReservation(token: String, id: String, request: UpdateReservationRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = ReservationUiState.Loading
            try {
                val result = repository.update(token, id, request)
                if (result != null) {
                    _uiState.value = ReservationUiState.Success("Reservation updated!")
                    onSuccess()
                } else {
                    _uiState.value = ReservationUiState.Error("Failed to update reservation.")
                }
            } catch (e: Exception) {
                _uiState.value = ReservationUiState.Error(e.message ?: "An error occurred.")
            }
        }
    }

    fun cancelReservation(token: String, id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = ReservationUiState.Loading
            try {
                val result = repository.cancel(token, id)
                if (result != null) {
                    _uiState.value = ReservationUiState.Success("Reservation cancelled.")
                    onSuccess()
                } else {
                    _uiState.value = ReservationUiState.Error("Failed to cancel reservation.")
                }
            } catch (e: Exception) {
                _uiState.value = ReservationUiState.Error(e.message ?: "An error occurred.")
            }
        }
    }

    fun approveReservation(token: String, id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = ReservationUiState.Loading
            try {
                val result = repository.approve(token, id)
                if (result != null) {
                    _uiState.value = ReservationUiState.Success("Reservation completed!")
                    onSuccess()
                } else {
                    _uiState.value = ReservationUiState.Error("Failed to complete reservation.")
                }
            } catch (e: Exception) {
                _uiState.value = ReservationUiState.Error(e.message ?: "An error occurred.")
            }
        }
    }

    fun resetState() {
        _uiState.value = ReservationUiState.Idle
    }
}
