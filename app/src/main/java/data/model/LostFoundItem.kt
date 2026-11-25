
package com.abel.lostandfound.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class LostFoundItem(
    @DocumentId
    val id: String = "",

    val ownerId: String = "",

    val title: String = "",

    val description: String = "",

    val location: String = "",

    val categoryId: String = "",

    val categoryName: String = "",

    val itemType: String = "",

    val dateOccurred: Timestamp? = null,

    val createdAt: Timestamp? = null,

    val updatedAt: Timestamp? = null
)
