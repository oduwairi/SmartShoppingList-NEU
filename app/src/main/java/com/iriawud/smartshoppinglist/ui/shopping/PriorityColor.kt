package com.iriawud.smartshoppinglist.ui.shopping

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.iriawud.smartshoppinglist.R

enum class PriorityColor(val colorRes: Int, val label: String) {
    LOW(R.color.priority_low, "Low"),
    MEDIUM(R.color.priority_medium, "Medium"),
    HIGH(R.color.priority_high, "High");

    companion object {
        fun from(priority: Int): PriorityColor {
            return when (priority) {
                in 1..3 -> LOW
                in 4..7 -> MEDIUM
                in 8..10 -> HIGH
                else -> LOW // Default or handle invalid values
            }
        }
    }

    // Method to set the priority text with color
    fun applyToTextView(textView: TextView) {
        val fullText = "Priority: $label"
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf(label)

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(textView.context, colorRes)),
            startIndex,
            startIndex + label.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable
    }
}
