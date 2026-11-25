
package com.abel.lostandfound.data.repository

import com.abel.lostandfound.data.model.ItemClaim
import com.abel.lostandfound.data.model.LostFoundCategory
import com.abel.lostandfound.data.model.LostFoundItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseLostFoundRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : LostFoundRepository {

    private val itemsCollection = firestore.collection("items")
    private val categoriesCollection = firestore.collection("categories")
    private val claimsCollection = firestore.collection("claims")

    override fun currentUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun observeMyItems(): Flow<List<LostFoundItem>> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = itemsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val items = snapshot?.toObjects(LostFoundItem::class.java)
                ?: emptyList()

            trySend(items)
        }

        awaitClose { listener.remove() }
    }

    override fun observeMyClaims(): Flow<List<ItemClaim>> = callbackFlow {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = claimsCollection
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val claims = snapshot?.toObjects(ItemClaim::class.java)
                    ?: emptyList()

                trySend(claims)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun submitClaim(item: LostFoundItem): Result<Unit> =
        runCatching {
            val user = auth.currentUser
                ?: error("User is not authenticated.")

            require(item.itemType == "found") {
                "Only found items can be claimed."
            }

            require(item.ownerId != user.uid) {
                "You cannot claim your own item."
            }

            val data = hashMapOf(
                "itemId" to item.id,
                "itemTitle" to item.title,
                "ownerId" to item.ownerId,
                "claimantId" to user.uid,
                "claimantEmail" to (user.email ?: "Unknown email"),
                "createdAt" to FieldValue.serverTimestamp()
            )

            claimsCollection.add(data).await()
        }

    override suspend fun getItem(id: String): Result<LostFoundItem> =
        runCatching {
            val document = itemsCollection.document(id).get().await()

            require(document.exists()) {
                "Item not found."
            }

            document.toObject(LostFoundItem::class.java)
                ?: error("Could not read item.")
        }

    override suspend fun getCategories(): Result<List<LostFoundCategory>> =
        runCatching {
            val snapshot = categoriesCollection.get().await()
            snapshot.toObjects(LostFoundCategory::class.java)
        }

    override suspend fun addItem(item: LostFoundItem): Result<String> =
        runCatching {
            val userId = auth.currentUser?.uid
                ?: error("User is not authenticated.")

            val data = hashMapOf(
                "ownerId" to userId,
                "title" to item.title,
                "description" to item.description,
                "location" to item.location,
                "categoryId" to item.categoryId,
                "categoryName" to item.categoryName,
                "itemType" to item.itemType,
                "dateOccurred" to item.dateOccurred,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val document = itemsCollection.add(data).await()
            document.id
        }

    override suspend fun updateItem(
        id: String,
        item: LostFoundItem
    ): Result<Unit> =
        runCatching {
            val userId = auth.currentUser?.uid
                ?: error("User is not authenticated.")

            val document = itemsCollection.document(id)
            val snapshot = document.get().await()

            require(
                snapshot.exists() &&
                        snapshot.getString("ownerId") == userId
            ) {
                "Item not found or access denied."
            }

            val updates = mapOf(
                "title" to item.title,
                "description" to item.description,
                "location" to item.location,
                "categoryId" to item.categoryId,
                "categoryName" to item.categoryName,
                "itemType" to item.itemType,
                "dateOccurred" to item.dateOccurred,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            document.update(updates).await()
        }

    override suspend fun deleteItem(id: String): Result<Unit> =
        runCatching {
            val userId = auth.currentUser?.uid
                ?: error("User is not authenticated.")

            val document = itemsCollection.document(id)
            val snapshot = document.get().await()

            require(
                snapshot.exists() &&
                        snapshot.getString("ownerId") == userId
            ) {
                "Item not found or access denied."
            }

            document.delete().await()
        }
}
