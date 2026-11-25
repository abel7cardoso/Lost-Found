
package com.abel.lostandfound.data.model

import com.google.firebase.firestore.DocumentId

data class LostFoundCategory(
    @DocumentId
    val id: String = "",
    val name: String = ""
)
