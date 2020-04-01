package com.nutrition.express.util

import androidx.preference.PreferenceManager
import com.nutrition.express.application.TumbApp.Companion.app

/**
 * Created by huang on 9/23/16.
 */
fun putString(key: String, value: String?) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(app)
    val editor = prefs.edit()
    editor.putString(key, value)
    editor.apply()
}

fun getString(key: String): String? {
    val prefs = PreferenceManager.getDefaultSharedPreferences(app)
    return prefs.getString(key, null)
}

fun putInt(key: String, value: Int) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(app)
    val editor = prefs.edit()
    editor.putInt(key, value)
    editor.apply()
}

fun getInt(key: String): Int {
    val prefs = PreferenceManager.getDefaultSharedPreferences(app)
    return prefs.getInt(key, 0)
}

fun putBoolean(key: String, value: Boolean) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(app)
    val editor = prefs.edit()
    editor.putBoolean(key, value)
    editor.apply()
}

fun getBoolean(key: String, value: Boolean): Boolean {
    val prefs = PreferenceManager.getDefaultSharedPreferences(app)
    return prefs.getBoolean(key, value)
}

