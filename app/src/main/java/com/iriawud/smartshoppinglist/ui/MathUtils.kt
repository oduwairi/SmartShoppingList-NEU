package com.iriawud.smartshoppinglist.ui.inventory

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MathUtils {

    /**
     * Calculates the frequency between two dates in days, weeks, or months.
     * @param stockedAt The stocked date as a string.
     * @param restockDate The restock date as a string.
     * @return Frequency as a string.
     */
    fun calculateFrequency(stockedAt: String, restockDate: String?): String {
        if (restockDate.isNullOrEmpty()) return "Not set"

        return try {
            val simpleStockedAt = convertToSimpleDateFormat(stockedAt)
            val simpleRestockDate = convertToSimpleDateFormat(restockDate)

            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val stockedDate = formatter.parse(simpleStockedAt)
            val restockDateParsed = formatter.parse(simpleRestockDate)

            if (stockedDate != null && restockDateParsed != null) {
                val differenceInMillis = restockDateParsed.time - stockedDate.time
                val differenceInDays = (differenceInMillis / (1000 * 60 * 60 * 24)).toInt()

                when {
                    differenceInDays < 7 -> "$differenceInDays per day"
                    differenceInDays < 30 -> "${differenceInDays / 7} per week"
                    else -> "${differenceInDays / 30} per month"
                }
            } else {
                "Not set"
            }
        } catch (e: Exception) {
            Log.e("MathUtils", "Error calculating frequency: ${e.message}", e)
            "Not set"
        }
    }

    /**
     * Calculates the restock date based on a frequency and a start date.
     * @param stockedAt The stocked date as a string.
     * @param frequency The frequency as a string.
     * @return The calculated restock date as a string.
     */
    fun calculateRestockDate(stockedAt: String, frequency: String): String? {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        return try {
            val stockedDate = formatter.parse(stockedAt)
            val frequencyRegex = Regex("(\\d+) per (\\w+)")
            val matchResult = frequencyRegex.matchEntire(frequency)

            if (stockedDate != null && matchResult != null) {
                val (amount, unit) = matchResult.destructured
                val daysToAdd = when (unit.lowercase()) {
                    "day", "days" -> amount.toInt()
                    "week", "weeks" -> amount.toInt() * 7
                    "month", "months" -> amount.toInt() * 30
                    else -> throw IllegalArgumentException("Frequency format not recognized")
                }
                val restockDate = Date(stockedDate.time + daysToAdd * 24 * 60 * 60 * 1000L)
                formatter.format(restockDate)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("MathUtils", "Error calculating restock date: ${e.message}", e)
            null
        }
    }

    /**
     * Converts a date string from RFC 1123 format to "yyyy-MM-dd HH:mm:ss" format.
     * @param dateString The date string in RFC 1123 format.
     * @return The formatted date string.
     */
    fun convertToSimpleDateFormat(dateString: String?): String {
        if (dateString.isNullOrBlank()) {
            return getCurrentTimestamp() // Fallback to current timestamp
        }

        return try {
            val rfc1123Formatter = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            val date = rfc1123Formatter.parse(dateString)

            val simpleFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            simpleFormatter.format(date!!)
        } catch (e: Exception) {
            Log.e("MathUtils", "Error converting date format: ${e.message}", e)
            getCurrentTimestamp() // Fallback to current timestamp
        }
    }

    /**
     * Gets the current timestamp in "yyyy-MM-dd HH:mm:ss" format.
     * @return The current timestamp as a string.
     */
    fun getCurrentTimestamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }

    fun dpToPx(dp: Float, context: Context): Int {
        return (dp * (context.resources.displayMetrics.densityDpi.toFloat() / 160f)).toInt()
    }
}
