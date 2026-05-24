package com.zenbuddy.data.repository

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zenbuddy.domain.model.User
import com.zenbuddy.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : AuthRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _currentUser = MutableStateFlow(loadCurrentUser())

    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    override fun isLoggedIn(): Boolean = _currentUser.value != null

    override fun getCurrentUserId(): String? = _currentUser.value?.uid

    override suspend fun loginWithDisplayName(displayName: String, password: String): Result<User> {
        val normalizedName = displayName.trim().lowercase()
        val account = loadAccounts().firstOrNull { it.displayNameLower == normalizedName }
            ?: return Result.failure(Exception("No account found with that display name"))

        if (account.passwordHash != passwordHash(password, account.salt)) {
            return Result.failure(Exception("Incorrect password"))
        }

        val user = account.toUser()
        saveCurrentUser(user)
        return Result.success(user)
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        val cleanEmail = email.trim()
        val cleanName = displayName.trim()
        val normalizedEmail = cleanEmail.lowercase()
        val normalizedName = cleanName.lowercase()
        val accounts = loadAccounts()

        if (accounts.any { it.email.lowercase() == normalizedEmail }) {
            return Result.failure(Exception("Email is already registered"))
        }
        if (accounts.any { it.displayNameLower == normalizedName }) {
            return Result.failure(Exception("Display name '$cleanName' is already taken"))
        }

        val salt = UUID.randomUUID().toString()
        val account = LocalAccount(
            uid = UUID.randomUUID().toString(),
            email = cleanEmail,
            displayName = cleanName,
            displayNameLower = normalizedName,
            salt = salt,
            passwordHash = passwordHash(password, salt),
            createdAt = System.currentTimeMillis()
        )

        saveAccounts(accounts + account)
        val user = account.toUser()
        saveCurrentUser(user)
        return Result.success(user)
    }

    override suspend fun logout() {
        prefs.edit { remove(KEY_CURRENT_USER_ID) }
        _currentUser.value = null
    }

    private fun loadCurrentUser(): User? {
        val uid = prefs.getString(KEY_CURRENT_USER_ID, null) ?: return null
        return loadAccounts().firstOrNull { it.uid == uid }?.toUser()
    }

    private fun saveCurrentUser(user: User) {
        prefs.edit { putString(KEY_CURRENT_USER_ID, user.uid) }
        _currentUser.value = user
    }

    private fun loadAccounts(): List<LocalAccount> {
        val json = prefs.getString(KEY_ACCOUNTS, null) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<LocalAccount>>(json, object : TypeToken<List<LocalAccount>>() {}.type)
        }.getOrDefault(emptyList())
    }

    private fun saveAccounts(accounts: List<LocalAccount>) {
        prefs.edit { putString(KEY_ACCOUNTS, gson.toJson(accounts)) }
    }

    private fun passwordHash(password: String, salt: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("$salt:$password".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun LocalAccount.toUser() = User(
        uid = uid,
        email = email,
        displayName = displayName
    )

    private data class LocalAccount(
        val uid: String,
        val email: String,
        val displayName: String,
        val displayNameLower: String,
        val salt: String,
        val passwordHash: String,
        val createdAt: Long
    )

    companion object {
        const val PREFS_NAME = "zenbuddy_prefs"
        const val KEY_CURRENT_USER_ID = "auth_current_user_id"
        private const val KEY_ACCOUNTS = "auth_accounts"
    }
}
