
package com.abel.lostandfound.data.repository

import com.abel.lostandfound.data.model.ItemClaim
import com.abel.lostandfound.data.model.LostFoundCategory
import com.abel.lostandfound.data.model.LostFoundItem
import kotlinx.coroutines.flow.Flow

interface LostFoundRepository {

    fun observeMyItems(): Flow<List<LostFoundItem>>

    fun observeMyClaims(): Flow<List<ItemClaim>>

    fun currentUserId(): String?

    suspend fun submitClaim(item: LostFoundItem): Result<Unit>

    suspend fun getItem(id: String): Result<LostFoundItem>

    suspend fun getCategories(): Result<List<LostFoundCategory>>

    suspend fun addItem(item: LostFoundItem): Result<String>

    suspend fun updateItem(id: String, item: LostFoundItem): Result<Unit>

    suspend fun deleteItem(id: String): Result<Unit>
}
