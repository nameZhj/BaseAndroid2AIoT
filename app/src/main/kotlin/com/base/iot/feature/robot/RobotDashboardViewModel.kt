package com.base.iot.feature.robot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RobotDashboardViewModel @Inject constructor(
    repository: RobotDashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RobotDashboardSampleData.state)
    val uiState: StateFlow<RobotDashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.dashboard.collect { state ->
                _uiState.value = state
            }
        }
    }
}
