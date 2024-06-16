package com.bangkit.naraspeak.helper

sealed class UserResult<out R> private constructor() {
    data class Name<out N>(val name: N) : UserResult<N>()
    class Gender<out G>(val gender: G) : UserResult<G>()
    class Level<out L>(val level: L) : UserResult<L>()
    class Failed(val error: String) : UserResult<Nothing>()
    object Loading : UserResult<Nothing>()
}