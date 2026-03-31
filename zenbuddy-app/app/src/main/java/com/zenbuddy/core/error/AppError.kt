package com.zenbuddy.core.error

sealed class AppError(val message: String) {
    class NetworkError(msg: String) : AppError(msg)
    class DatabaseError(msg: String) : AppError(msg)
    class AuthError(msg: String) : AppError(msg)
    class AiError(msg: String) : AppError(msg)
    class Unknown(msg: String) : AppError(msg)
}
