package com.belajar.naraspeak.helper

sealed class Result<out R> private constructor() {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failed(val error: String) : Result<Nothing>()
    object Loading : Result<Nothing>()

}