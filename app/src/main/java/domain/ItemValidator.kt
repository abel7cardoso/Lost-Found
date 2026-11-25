
package com.abel.lostandfound.domain

object ItemValidator {

    fun validate(
        title: String,
        description: String,
        location: String,
        categoryId: String,
        itemType: String
    ): Map<String, String> {

        val errors = mutableMapOf<String, String>()

        if (title.isBlank()) {
            errors["title"] = "Title cannot be empty."
        } else if (title.length > 80) {
            errors["title"] = "Title cannot exceed 80 characters."
        }

        if (description.isBlank()) {
            errors["description"] = "Description cannot be empty."
        } else if (description.length > 500) {
            errors["description"] = "Description cannot exceed 500 characters."
        }

        if (location.isBlank()) {
            errors["location"] = "Location cannot be empty."
        }

        if (categoryId.isBlank()) {
            errors["categoryId"] = "Please select a category."
        }

        if (itemType !in listOf("lost", "found")) {
            errors["itemType"] = "Please select Lost or Found."
        }

        // Cross-field validation
        if (
            title.isNotBlank() &&
            description.isNotBlank() &&
            title.trim().equals(description.trim(), ignoreCase = true)
        ) {
            errors["description"] =
                "Description must provide more detail than the title."
        }

        return errors
    }
}
