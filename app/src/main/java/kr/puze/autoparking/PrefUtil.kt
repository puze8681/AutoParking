package kr.puze.autoparking

import android.content.Context
import android.content.SharedPreferences

class PrefUtil(context: Context, date: String) {
    var date = date
    private var preferences: SharedPreferences = context.getSharedPreferences("Price", Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = preferences.edit()

    var todayPrice: Int
        get() = preferences.getInt(date, 0)
        set(value) {
            editor.putInt(date, value)
            editor.apply()
        }
}