package com.abel.lostandfound.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abel.lostandfound.data.FirebaseAuthRepository
import com.abel.lostandfound.domain.AuthValidator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val error: String? = null,
    val isLoading: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val repository = FirebaseAuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _isLoggedIn = MutableStateFlow(Firebase.auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    init {
        Firebase.auth.addAuthStateListener { auth ->
            _isLoggedIn.value = auth.currentUser != null
        }
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, error = null)
    }

    fun login() {
        val state = _uiState.value
        val emailError = AuthValidator.validateEmail(state.email)
        val passwordError = AuthValidator.validatePassword(state.password)

        if (emailError != null || passwordError != null) {
            _uiState.value = state.copy(error = emailError ?: passwordError)
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = repository.login(state.email, state.password)
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = mapFirebaseError(exception as Exception)
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun register() {
        val state = _uiState.value
        val emailError = AuthValidator.validateEmail(state.email)
        val passwordError = AuthValidator.validatePassword(state.password)
        val confirmError = AuthValidator.validatePasswordConfirmation(state.password, state.confirmPassword)

        if (emailError != null || passwordError != null || confirmError != null) {
            _uiState.value = state.copy(error = emailError ?: passwordError ?: confirmError)
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = repository.register(state.email, state.password)
            result.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = mapFirebaseError(exception as Exception)
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun logout() {
        repository.logout()
    }

    private fun mapFirebaseError(exception: Exception): String {
        return when {
            exception.message?.contains("email address is already in use") == true ->
                "An account with this email already exists."
            exception.message?.contains("password is invalid") == true ||
                    exception.message?.contains("credential is incorrect") == true ->
                "Incorrect email or password."
            else -> "An error occurred: ${exception.message}"
        }
    }
}