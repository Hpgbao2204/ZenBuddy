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
import com.zenbuddy.app.BuildConfig
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

    override fun isLoggedIn(): Boolean {
        val user = firebaseAuth.currentUser ?: return false
        return if (BuildConfig.DEBUG) true else user.isEmailVerified
    }

    override fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    override suspend fun loginWithDisplayName(displayName: String, password: String): Result<User> {
        return try {
            // Lookup email from Firestore by displayName (case-insensitive)
            val snapshot = firestore.collection("users")
                .whereEqualTo("displayNameLower", displayName.trim().lowercase())
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("No account found with that display name"))
            }

            val email = snapshot.documents[0].getString("email")
                ?: return Result.failure(Exception("Account data is corrupted"))

            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Login failed"))

            if (!BuildConfig.DEBUG && !firebaseUser.isEmailVerified) {
                // Resend verification email
                firebaseUser.sendEmailVerification().await()
                firebaseAuth.signOut()
                return Result.failure(Exception("Email not verified. A new verification email has been sent. Please check your inbox."))
            }

            Result.success(firebaseUser.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return try {
            // Check if displayName is already taken (case-insensitive)
            val existing = firestore.collection("users")
                .whereEqualTo("displayNameLower", displayName.trim().lowercase())
                .limit(1)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.failure(Exception("Display name '${displayName.trim()}' is already taken"))
            }

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
            "displayName" to displayName.trim(),
            "displayNameLower" to displayName.trim().lowercase(),
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("users")
            .document(firebaseUser.uid)
            .set(userDoc)
            .await()

        if (!BuildConfig.DEBUG) {
            // Sign out so user must verify email before accessing app
            firebaseAuth.signOut()
        }

        Result.success(User(uid = firebaseUser.uid, email = email, displayName = displayName.trim()))
        } catch (e: Exception) {
            Result.failure(e)
        }
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
