package com.zenbuddy.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.zenbuddy.domain.model.User
import com.zenbuddy.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toDomain())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    override suspend fun login(email: String, password: String): Result<User> = try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = result.user?.toDomain()
            ?: return Result.failure(Exception("Login failed"))
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<User> = try {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user
            ?: return Result.failure(Exception("Registration failed"))

        // Update display name
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        firebaseUser.updateProfile(profileUpdates).await()

        // Send email verification
        firebaseUser.sendEmailVerification().await()

        // Create user document in Firestore
        val userDoc = mapOf(
            "uid" to firebaseUser.uid,
            "email" to email,
            "displayName" to displayName,
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("users")
            .document(firebaseUser.uid)
            .set(userDoc)
            .await()

        Result.success(firebaseUser.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    private fun com.google.firebase.auth.FirebaseUser.toDomain() = User(
        uid = uid,
        email = email ?: "",
        displayName = displayName
    )
}
