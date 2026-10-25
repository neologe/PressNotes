package com.pressnotes.app

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var prefs: Prefs
    private var notes: MutableList<String> = mutableListOf()
    private lateinit var list: LinearLayout
    private lateinit var input: EditText
    private var draft = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
    }

    override fun onResume() {
        super.onResume()
        build()
    }

    override fun onPause() {
        super.onPause()
        if (::input.isInitialized) draft = input.text.toString()
    }

    private fun build() {
        notes = prefs.loadNotes()
        val bg = prefs.appBg
        val fg = contrastOn(bg)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(bg)
        root.fitsSystemWindows = true

        val header = LinearLayout(this)
        header.gravity = Gravity.CENTER_VERTICAL
        header.setPadding(dp(16), dp(8), dp(8), dp(8))
        val title = TextView(this)
        title.text = "PressNotes"
        title.setTextColor(fg)
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        title.typeface = Typeface.DEFAULT_BOLD
        header.addView(title, lp(0, WRAP, 1f))
        header.addView(
            pill("⚙ Options", fg, bg) {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            },
            lp(WRAP, dp(48))
        )
        root.addView(header, lp(MATCH, WRAP))

        list = LinearLayout(this)
        list.orientation = LinearLayout.VERTICAL
        list.setPadding(dp(12), dp(4), dp(12), dp(12))
        val scroll = ScrollView(this)
        scroll.addView(list)
        root.addView(scroll, lp(MATCH, 0, 1f))

        input = EditText(this)
        input.hint = "New note…"
        input.setText(draft)
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        input.setTextColor(Color.BLACK)
        input.setHintTextColor(Color.GRAY)
        input.background = rounded(Color.WHITE, dp(12).toFloat())
        input.setPadding(dp(14), dp(10), dp(14), dp(10))
        input.maxLines = 4
        input.inputType = InputType.TYPE_CLASS_TEXT or
            InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        val add = pill("ADD", 0xFFFF6D00.toInt(), Color.BLACK) { addNote() }

        val bar = LinearLayout(this)
        bar.gravity = Gravity.CENTER_VERTICAL
        bar.setPadding(dp(10), dp(8), dp(10), dp(10))
        bar.addView(input, lp(0, WRAP, 1f))
        val addLp = lp(dp(90), dp(56))
        addLp.leftMargin = dp(8)
        bar.addView(add, addLp)
        root.addView(bar, lp(MATCH, WRAP))

        setContentView(root)
        render()
    }

    private fun render() {
        list.removeAllViews()
        val appFg = contrastOn(prefs.appBg)
        if (notes.isEmpty()) {
            val empty = TextView(this)
            empty.text = "No notes yet.\nType below and tap ADD."
            empty.setTextColor(appFg)
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            empty.setPadding(dp(8), dp(24), dp(8), dp(8))
            list.addView(empty)
        }
        notes.forEachIndexed { i, body ->
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL

            val tv = TextView(this)
            tv.text = body
            tv.setTextColor(prefs.noteText)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.fontSize.toFloat())
            tv.typeface = Typeface.DEFAULT_BOLD
            tv.setPadding(dp(16), dp(14), dp(16), dp(14))
            tv.background = rounded(prefs.noteBg, dp(14).toFloat())
            tv.setOnClickListener { edit(i) }
            row.addView(tv, lp(0, WRAP, 1f))

            val copy = pill("COPY", appFg, prefs.appBg) {}
            copy.setOnClickListener { copyText(body, copy) }
            val copyLp = lp(dp(96), MATCH)
            copyLp.leftMargin = dp(8)
            row.addView(copy, copyLp)

            val rowLp = lp(MATCH, WRAP)
            rowLp.topMargin = dp(10)
            list.addView(row, rowLp)
        }
    }

    private fun copyText(t: String, b: Button) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("note", t))
        b.text = "COPIED ✓"
        b.postDelayed({ b.text = "COPY" }, 1200)
    }

    private fun addNote() {
        val s = input.text.toString().trim()
        if (s.isEmpty()) return
        notes.add(0, s)
        prefs.saveNotes(notes)
        input.setText("")
        draft = ""
        render()
    }

    private fun edit(i: Int) {
        val et = EditText(this)
        et.setText(notes[i])
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        et.setPadding(dp(20), dp(12), dp(20), dp(12))
        AlertDialog.Builder(this)
            .setTitle("Edit note")
            .setView(et)
            .setPositiveButton("Save") { _, _ ->
                val s = et.text.toString().trim()
                if (s.isEmpty()) notes.removeAt(i) else notes[i] = s
                prefs.saveNotes(notes)
                render()
            }
            .setNeutralButton("Delete") { _, _ ->
                notes.removeAt(i)
                prefs.saveNotes(notes)
                render()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
