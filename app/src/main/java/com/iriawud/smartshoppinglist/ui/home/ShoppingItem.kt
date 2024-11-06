package com.iriawud.smartshoppinglist.ui.home

import android.content.Context
import com.iriawud.smartshoppinglist.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ShoppingItem(
    val name: String,
    val quantity: String = "1 pcs",
    val category: String = "Uncategorized",
    val price: String = "Not set",
    val priority: Int = 5,
    val imageUrl: String = name.lowercase(),
    val frequency: String = "Not set",
    val createdAt: String = getCurrentTimestamp() // Use a helper function to get the timestamp
) {
    companion object {
        fun getCurrentTimestamp(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return dateFormat.format(Date())
        }
    }

    // Compute days passed since 'createdAt'
    fun getDaysPassed(): Int {
        val currentDate = Date()
        val createdAtDate = getCreatedAtDate()
        val timePassedMillis = currentDate.time - createdAtDate.time
        return (timePassedMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    // Helper to parse 'createdAt' into a Date object
    private fun getCreatedAtDate(): Date {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.parse(createdAt)!!
    }

    fun getTimeLeft(): String {
        val daysPassed = getDaysPassed()

        // Parse frequency (e.g., "1 per week")
        val frequencyRegex = Regex("(\\d+) per (\\w+)")
        val matchResult = frequencyRegex.matchEntire(frequency)
        return if (matchResult != null) {
            val (amount, unit) = matchResult.destructured
            val frequencyInDays = when (unit.lowercase()) {
                "day", "days" -> amount.toInt()
                "week", "weeks" -> 7 / amount.toInt()
                "month", "months" -> 30 / amount.toInt()
                else -> return "Frequency not recognized"
            }

            val daysLeft = frequencyInDays - daysPassed

            // Convert daysLeft to appropriate time unit
            when {
                daysLeft >= 30 -> {
                    val months = daysLeft / 30
                    "${months} month${if (months > 1) "s" else ""}"
                }
                daysLeft >= 7 -> {
                    val weeks = daysLeft / 7
                    "${weeks} week${if (weeks > 1) "s" else ""}"
                }
                daysLeft > 0 -> {
                    "${daysLeft} day${if (daysLeft > 1) "s" else ""}"
                }
                daysLeft < 0 -> {
                    val overdueDays = -daysLeft
                    "${overdueDays} day${if (overdueDays > 1) "s" else ""} overdue"
                }
                else -> "Due today"
            }
        } else {
            "Not set"
        }
    }

    // Parse quantity as a numeric value
    fun getFullQuantity(): Double {
        return quantity.split(" ")[0].toDoubleOrNull() ?: 0.0
    }

    // Calculate the consumption rate (quantity per day)
    fun getConsumptionRate(): Double {
        val frequencyRegex = Regex("(\\d+) per (\\w+)")
        val matchResult = frequencyRegex.matchEntire(frequency)
        return if (matchResult != null) {
            val (amount, unit) = matchResult.destructured
            val frequencyInDays = when (unit.lowercase()) {
                "day", "days" -> 1.0
                "week", "weeks" -> 7.0
                "month", "months" -> 30.0 // Approximation
                else -> 1.0
            }
            getFullQuantity() * amount.toDouble() / (frequencyInDays)
        } else {
            0.0
        }
    }

    // Calculate the remaining amount
    fun getAmountLeft(): Double {
        val daysPassed = getDaysPassed()
        val consumptionRate = getConsumptionRate()
        return (getFullQuantity() - (daysPassed * consumptionRate)).coerceAtLeast(0.0)
    }

    // Calculate bar width as a percentage of the max width
    fun getBarWidth(maxBarWidth: Int): Int {
        val amountLeft = getAmountLeft()
        val fullQuantity = getFullQuantity()
        return (maxBarWidth * (amountLeft / fullQuantity)).toInt().coerceAtLeast(0)
    }

    // Get bar color based on the amount left
    fun getBarColor(context: Context): Int {
        val amountLeft = getAmountLeft()
        val fullQuantity = getFullQuantity()
        return when {
            amountLeft > fullQuantity * 0.5 -> context.getColor(R.color.amount_high) // More than 50% left
            amountLeft > fullQuantity * 0.2 -> context.getColor(R.color.amount_medium) // 20-50% left
            else -> context.getColor(R.color.amount_low) // Less than 20% left
        }
    }


}





