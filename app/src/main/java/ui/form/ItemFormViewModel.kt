
package com.abel.lostandfound.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abel.lostandfound.data.model.LostFoundCategory
import com.abel.lostandfound.data.model.LostFoundItem
import com.abel.lostandfound.data.repository.LostFoundRepository
import com.abel.lostandfound.domain.ItemValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ItemFormUiState(
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val itemType: String = "lost",
    val categories: List<LostFoundCategory> = emptyList(),
    val errors: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

class ItemFormViewModel(
    private val repository: LostFoundRepository,
    private val itemId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemFormUiState())
    val uiState: StateFlow<ItemFormUiState> = _uiState

    init {
        loadCategories()
        if (itemId != null) {
            loadItem(itemId)
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories()
                .onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Failed to load categories."
                    )
                }
        }
    }

    private fun loadItem(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getItem(id)
                .onSuccess { item ->
                    _uiState.value = _uiState.value.copy(
                        title = item.title,
                        description = item.description,
                        location = item.location,
                        categoryId = item.categoryId,
                        categoryName = item.categoryName,
                        itemType = item.itemType,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load item."
                    )
                }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value, errors = emptyMap())
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value, errors = emptyMap())
    }

    fun onLocationChange(value: String) {
        _uiState.value = _uiState.value.copy(location = value, errors = emptyMap())
    }

    fun onCategoryChange(category: LostFoundCategory) {
        _uiState.value = _uiState.value.copy(
            categoryId = category.id,
            categoryName = category.name,
            errors = emptyMap()
        )
    }

    fun onItemTypeChange(value: String) {
        _uiState.value = _uiState.value.copy(itemType = value, errors = emptyMap())
    }

    fun save() {
        val state = _uiState.value

        val errors = ItemValidator.validate(
            title = state.title,
            description = state.description,
            location = state.location,
            categoryId = state.categoryId,
            itemType = state.itemType
        )

        if (errors.isNotEmpty()) {
            _uiState.value = state.copy(errors = errors)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
                isSaving = true,
                errors = emptyMap(),
                errorMessage = null
            )

            val item = LostFoundItem(
                title = state.title.trim(),
                description = state.description.trim(),
                location = state.location.trim(),
                categoryId = state.categoryId,
                categoryName = state.categoryName,
                itemType = state.itemType
            )

            val result = if (itemId == null) {
                repository.addItem(item).map { Unit }
            } else {
                repository.updateItem(itemId, item)
            }

            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isSaved = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = exception.message ?: "Failed to save item."
                    )
                }
        }
    }
}
