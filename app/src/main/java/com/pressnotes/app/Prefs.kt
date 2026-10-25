package com.pressnotes.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import org.json.JSONArray

const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

fun lp(w: Int, h: Int, weight: Float = 0f) = LinearLayout.LayoutParams(w, h, weight)
fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

fun contrastOn(c: Int): Int {
    val l = 0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c)
    return if (l > 140) Color.BLACK else Color.WHITE
}

fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
    setColor(color)
    cornerRadius = radius
}

fun Context.pill(label: String, bgColor: Int, textColor: Int, onClick: () -> Unit): Button {
    val b = Button(this)
    b.text = label
    b.isAllCaps = false
    b.setTextColor(textColor)
    b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
    b.typeface = Typeface.DEFAULT_BOLD
    b.setPadding(dp(6), 0, dp(6), 0)
    b.background = rounded(bgColor, dp(12).toFloat())
    b.setOnClickListener { onClick() }
    return b
}

class Prefs(ctx: Context) {
    private val p = ctx.getSharedPreferences("pressnotes", Context.MODE_PRIVATE)

    var fontSize: Int
        get() = p.getInt("font", 26)
        set(v) { p.edit().putInt("font", v).apply() }
    var noteText: Int
        get() = p.getInt("noteText", 0xFF000000.toInt())
        set(v) { p.edit().putInt("noteText", v).apply() }
    var noteBg: Int
        get() = p.getInt("noteBg", 0xFFFFD600.toInt())
        set(v) { p.edit().putInt("noteBg", v).apply() }
    var appBg: Int
        get() = p.getInt("appBg", 0xFF101820.toInt())
        set(v) { p.edit().putInt("appBg", v).apply() }

    fun reset() {
        p.edit().remove("font").remove("noteText").remove("noteBg").remove("appBg").apply()
    }

    fun loadNotes(): MutableList<String> {
        val a = JSONArray(p.getString("notes", "[]"))
        return MutableList(a.length()) { a.getString(it) }
    }

    fun saveNotes(l: List<String>) {
        p.edit().putString("notes", JSONArray(l).toString()).apply()
    }
}
