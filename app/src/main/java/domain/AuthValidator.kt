package com.abel.lostandfound.domain

object AuthValidator {
    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email cannot be empty"
        if (!email.contains("@")) return "Invalid email format"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.length < 6) return "Password must be at least 6 characters"
        return null
    }

    fun validatePasswordConfirmation(password: String, confirmPassword: String): String? {
        if (password != confirmPassword) return "Passwords do not match"
        return null
    }
}