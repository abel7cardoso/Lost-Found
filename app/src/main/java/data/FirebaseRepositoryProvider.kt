
package com.abel.lostandfound.data

import com.abel.lostandfound.data.repository.FirebaseLostFoundRepository
import com.abel.lostandfound.data.repository.LostFoundRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseRepositoryProvider {

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository()
    }

    val lostFoundRepository: LostFoundRepository by lazy {
        FirebaseLostFoundRepository(
            FirebaseFirestore.getInstance(),
            FirebaseAuth.getInstance()
        )
    }
}
