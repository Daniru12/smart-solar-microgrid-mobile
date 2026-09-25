package com.example.smartsolar.features.microgrid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsolar.features.microgrid.models.EnergySlot
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.features.microgrid.repository.MicrogridRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class MicrogridViewModel(
    private val repository: MicrogridRepository
) : ViewModel() {

    private val _stationsState = MutableStateFlow<UiState<List<Station>>>(UiState.Loading)
    val stationsState: StateFlow<UiState<List<Station>>> = _stationsState.asStateFlow()

    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    private val _selectedStationState = MutableStateFlow<UiState<Station>>(UiState.Loading)
    val selectedStationState: StateFlow<UiState<Station>> = _selectedStationState.asStateFlow()

    private val _slotsState = MutableStateFlow<UiState<List<EnergySlot>>>(UiState.Loading)
    val slotsState: StateFlow<UiState<List<EnergySlot>>> = _slotsState.asStateFlow()

    private val _slots = MutableStateFlow<List<EnergySlot>>(emptyList())
    val slots: StateFlow<List<EnergySlot>> = _slots.asStateFlow()

    fun loadStations() {
        viewModelScope.launch {
            _stationsState.value = UiState.Loading
            repository.getStations()
                .onSuccess { stationList ->
                    _stationsState.value = UiState.Success(stationList)
                    _stations.value = stationList
                }
                .onFailure { error ->
                    _stationsState.value = UiState.Error(error.message ?: "Failed to load stations")
                }
        }
    }

    fun loadStationDetails(stationId: String) {
        viewModelScope.launch {
            _selectedStationState.value = UiState.Loading
            repository.getStationById(stationId)
                .onSuccess { station ->
                    _selectedStationState.value = UiState.Success(station)
                }
                .onFailure { error ->
                    _selectedStationState.value = UiState.Error(error.message ?: "Failed to load station")
                }
        }
    }

    fun loadSlots(stationId: String, date: String? = null) {
        viewModelScope.launch {
            _slotsState.value = UiState.Loading
            repository.getStationSlots(stationId, date)
                .onSuccess { slotList ->
                    _slotsState.value = UiState.Success(slotList)
                    _slots.value = slotList
                }
                .onFailure { error ->
                    _slotsState.value = UiState.Error(error.message ?: "Failed to load slots")
                }
        }
    }
}
