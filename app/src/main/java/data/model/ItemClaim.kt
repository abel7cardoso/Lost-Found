
package com.abel.lostandfound.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ItemClaim(
    @DocumentId
    val id: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val ownerId: String = "",
    val claimantId: String = "",
    val claimantEmail: String = "",
    val createdAt: Timestamp? = null
)
