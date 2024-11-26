package com.iriawud.smartshoppinglist.ui.shopping

import android.content.Context
import com.iriawud.smartshoppinglist.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Item(
    val id:Int ?= null,
    val name: String = "Unknown",
    var quantity: String = "1 pcs",
    var category: String = "Uncategorized",
    var price: String = "1 USD",
    var priority: Int = 5,
    var imageUrl: String = name.lowercase(),
    var frequency: String = "Not set",
    var createdAt: String = getCurrentTimestamp(),
    private var explicitStartingPercent: Int? = null
) {
    companion object {
        fun getCurrentTimestamp(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return dateFormat.format(Date())
        }
    }

    // Compute the percentage of the amount left or use the explicitly provided value
    val amountLeftPercent: Int
        get() {
            val startingPercent = explicitStartingPercent ?: 100
            return calculateDynamicPercentageLeft(startingPercent)
        }

    // Allow explicitly setting the remaining percentage
    fun setExplicitAmountLeftPercent(value: Int?) {
        explicitStartingPercent = value?.coerceIn(0, 100) // Ensure the value is between 0 and 100
    }

    // Dynamically calculate the remaining percentage
    private fun calculateDynamicPercentageLeft(startingPercent: Int): Int {
        val daysPassed = getDaysPassed()
        return try {
            val frequencyInDays = getFrequencyInDays()
            val percentReduction = (daysPassed / frequencyInDays * 100).toInt()
            (startingPercent - percentReduction).coerceIn(0, 100)
        } catch (e: IllegalArgumentException) {
            startingPercent // Fallback to starting percent if frequency is invalid
        }
    }


    // Compute days passed since 'createdAt'
    private fun getDaysPassed(): Double {
        val currentDate = Date()
        val createdAtDate = getCreatedAtDate()
        val timePassedMillis = currentDate.time - createdAtDate.time
        return (timePassedMillis / (1000.0 * 60.0 * 60.0 * 24.0))
    }

    // Helper to parse 'createdAt' into a Date object
    private fun getCreatedAtDate(): Date {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.parse(createdAt)!!
    }

    fun getTimeLeft(): String {
        return try {
            val daysLeft = (getFrequencyInDays() - getDaysPassed()).toInt()

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
        } catch (e: IllegalArgumentException) {
            e.message ?: "Error calculating time left"
        }
    }


    private fun getFrequencyInDays(): Double {
        val frequencyRegex = Regex("(\\d+) per (\\w+)")
        val matchResult = frequencyRegex.matchEntire(frequency)

        return if (matchResult != null) {
            val (amount, unit) = matchResult.destructured
            when (unit.lowercase()) {
                "day", "days" -> 1.0 / amount.toDouble() // X per day
                "week", "weeks" -> 7.0 / amount.toDouble() // X per week
                "month", "months" -> 30.0 / amount.toDouble() // X per month
                else -> throw IllegalArgumentException("Frequency format not recognized")
            }
        } else {
            throw IllegalArgumentException("Not set")
        }
    }

    // Calculate bar width as a percentage of the max width
    fun getBarWidth(maxBarWidth: Int): Int {
        // Use amountLeftPercent (explicit or calculated)
        val barWidth = maxBarWidth * amountLeftPercent / 100
        return barWidth.coerceAtLeast(0) // Ensure width is not negative
    }


    // Get bar color based on the amount left
    fun getBarColor(context: Context): Int {
        // Use amountLeftPercent (explicit or calculated)
        return when {
            amountLeftPercent > 50 -> context.getColor(R.color.amount_high) // More than 50% left
            amountLeftPercent > 20 -> context.getColor(R.color.amount_medium) // 20-50% left
            else -> context.getColor(R.color.amount_low) // Less than 20% left
        }
    }
}





