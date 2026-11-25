
package com.abel.lostandfound.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abel.lostandfound.data.model.ItemClaim
import com.abel.lostandfound.data.model.LostFoundItem
import com.abel.lostandfound.data.repository.LostFoundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

data class HomeUiState(
    val items: List<LostFoundItem> = emptyList(),
    val claims: List<ItemClaim> = emptyList(),
    val currentUserId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val message: String? = null
)

class HomeViewModel(
    private val repository: LostFoundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(currentUserId = repository.currentUserId())
    )

    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        observeItems()
        observeClaims()
    }

    private fun observeItems() {
        viewModelScope.launch {
            repository.observeMyItems()
                .onStart {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        error = null
                    )
                }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load items."
                    )
                }
                .collect { items ->
                    _uiState.value = _uiState.value.copy(
                        items = items,
                        isLoading = false
                    )
                }
        }
    }

    private fun observeClaims() {
        viewModelScope.launch {
            repository.observeMyClaims()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to load claims."
                    )
                }
                .collect { claims ->
                    _uiState.value = _uiState.value.copy(claims = claims)
                }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            repository.deleteItem(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        message = "Item deleted successfully.",
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to delete item."
                    )
                }
        }
    }

    fun submitClaim(item: LostFoundItem) {
        viewModelScope.launch {
            repository.submitClaim(item)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        message = "Claim request sent to the owner.",
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to send claim request."
                    )
                }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
