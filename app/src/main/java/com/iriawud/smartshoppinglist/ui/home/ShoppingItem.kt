package com.iriawud.smartshoppinglist.ui.home

import com.iriawud.smartshoppinglist.R

data class ShoppingItem(
    val name:String,
    val quantity:String = "1 pc",
    val category: String,
    val price: String = "1 USD",
    val priority: Int = 5,
    val imageUrl: String? = null,
)

enum class PriorityColor(val colorRes: Int) {
    LOW(R.color.priority_low),       // Assume colorLowPriority is defined in colors.xml
    MEDIUM(R.color.priority_medium), // Assume colorMediumPriority is defined in colors.xml
    HIGH(R.color.priority_high);     // Assume colorHighPriority is defined in colors.xml

    companion object {
        fun from(priority: Int): PriorityColor {
            return when (priority) {
                in 1..3 -> LOW
                in 4..7 -> MEDIUM
                in 8..10 -> HIGH
                else -> LOW  // Default or consider throwing an exception if invalid
            }
        }
    }
}



