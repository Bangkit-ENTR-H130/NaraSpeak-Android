package com.bangkit.naraspeak.helper

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ProgressBar
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

private val date = SimpleDateFormat("yyyyMMMdd", Locale.getDefault()).format(Date())
private const val PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\\\d)(?=.*[$@$!%*#?&])[A-Za-z\\\\d$@$!%*#?&]{8,}$"

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPassword(password: String): Boolean {
    val pattern = Pattern.compile(PASSWORD_PATTERN)
    return pattern.matcher(password).matches()
}


fun createTemptFile(context: Context): File {
    val cacheDir = context.externalCacheDir
    return File.createTempFile(date, ".wav", cacheDir)
}

//fun isValidPassword(password: String?): Boolean {
//    val pattern: Pattern
//    val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
//    pattern = Pattern.compile(PASSWORD_PATTERN)
//    val matcher: Matcher = pattern.matcher(password)
//
//    return matcher.matches()
//}

fun showLoading(isLoading: Boolean, loadingView: ProgressBar) {
    loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
}