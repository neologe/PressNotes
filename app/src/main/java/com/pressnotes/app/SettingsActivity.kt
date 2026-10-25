package com.pressnotes.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView

class SettingsActivity : Activity() {
    private lateinit var prefs: Prefs
    private var sv: ScrollView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        build()
    }

    private fun build() {
        val keepY = sv?.scrollY ?: 0
        val bg = prefs.appBg
        val fg = contrastOn(bg)

        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(16), dp(12), dp(16), dp(28))

        fun label(t: String): TextView {
            val v = TextView(this)
            v.text = t
            v.setTextColor(fg)
            v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            v.typeface = Typeface.DEFAULT_BOLD
            v.setPadding(0, dp(18), 0, dp(8))
            return v
        }

        col.addView(label("Preview"))
        val preview = TextView(this)
        preview.text = "Call the plumber about the leak"
        preview.setTextColor(prefs.noteText)
        preview.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.fontSize.toFloat())
        preview.typeface = Typeface.DEFAULT_BOLD
        preview.setPadding(dp(16), dp(14), dp(16), dp(14))
        preview.background = rounded(prefs.noteBg, dp(14).toFloat())
        col.addView(preview)

        val sizeLabel = label("Font size: ${prefs.fontSize}")
        col.addView(sizeLabel)
        val sb = SeekBar(this)
        sb.max = 32
        sb.progress = prefs.fontSize - 16
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                val size = 16 + p
                prefs.fontSize = size
                preview.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
                sizeLabel.text = "Font size: $size"
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        col.addView(sb)

        fun swatches(title: String, colors: List<Long>, current: Int, set: (Int) -> Unit) {
            col.addView(label(title))
            val r = LinearLayout(this)
            colors.forEach { c ->
                val ci = c.toInt()
                val v = View(this)
                val d = GradientDrawable()
                d.shape = GradientDrawable.OVAL
                d.setColor(ci)
                if (ci == current) d.setStroke(dp(5), fg) else d.setStroke(dp(2), 0x66888888)
                v.background = d
                v.setOnClickListener { set(ci); build() }
                val l = lp(dp(44), dp(44))
                l.rightMargin = dp(8)
                r.addView(v, l)
            }
            col.addView(r)
        }

        swatches("Note background", listOf(0xFFFFD600, 0xFFFFFFFF, 0xFFFF9100, 0xFF00E5FF, 0xFFB2FF59, 0xFFFF80AB),
            prefs.noteBg) { prefs.noteBg = it }
        swatches("Note text", listOf(0xFF000000, 0xFFFFFFFF, 0xFF0D1B4C, 0xFF7F0000, 0xFF003300),
            prefs.noteText) { prefs.noteText = it }
        swatches("App background", listOf(0xFF000000, 0xFF101820, 0xFF303030, 0xFF0A2A5E, 0xFFFFFFFF),
            prefs.appBg) { prefs.appBg = it }

        col.addView(View(this), lp(MATCH, dp(24)))
        col.addView(pill("DONE", 0xFFFF6D00.toInt(), Color.BLACK) { finish() }, lp(MATCH, dp(56)))
        val resetLp = lp(MATCH, dp(48))
        resetLp.topMargin = dp(10)
        col.addView(pill("Reset to defaults", fg, bg) { prefs.reset(); build() }, resetLp)

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)
        scroll.fitsSystemWindows = true
        scroll.addView(col)
        sv = scroll
        setContentView(scroll)
        scroll.post { scroll.scrollTo(0, keepY) }
    }
}
