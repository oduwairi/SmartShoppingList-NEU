package com.iriawud.smartshoppinglist.ui.inventory

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.ui.CategoryRepository
import com.iriawud.smartshoppinglist.ui.GuiUtils
import com.iriawud.smartshoppinglist.ui.shopping.PriorityColor
import com.iriawud.smartshoppinglist.ui.shopping.ShoppingItem
import java.text.SimpleDateFormat
import java.util.Locale

class InventoryItemAdapter(private val items: List<ShoppingItem>) :
    RecyclerView.Adapter<InventoryItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.inventoryItemName)
        val itemImage : ImageView = itemView.findViewById(R.id.inventoryItemImage)
        val itemAddedDate :TextView = itemView.findViewById(R.id.inventoryDateAddedText)
        val itemPriority : TextView = itemView.findViewById(R.id.inventoryItemPriority)
        val itemTimeLeft : TextView = itemView.findViewById(R.id.inventoryTimeLeftText)
        val itemAmountLeftIndicator : CardView = itemView.findViewById(R.id.amountLeftIndicator)
        val itemCard: CardView = itemView.findViewById(R.id.inventoryItemCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        //set item name in xml layout
        val item = items[position]
        holder.itemName.text = item.name

        // Format and set item date added in XML layout
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) // e.g., "06 Nov 2024"
        val dateAdded = dateFormatter.format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.createdAt)!!)
        holder.itemAddedDate.text = "Added on: $dateAdded"

        //set item priority in xml layout
        val priorityColor = PriorityColor.from(item.priority)
        priorityColor.applyToTextView(holder.itemPriority)

        //set time left based on frequency if applicable
        holder.itemTimeLeft.text = "Time left: ${item.getTimeLeft()}"

        //set remaining quantity bar length and color
        holder.itemAmountLeftIndicator.post {
            val maxBarWidth = holder.itemAmountLeftIndicator.width // Max width of the CardView

            // Calculate and set bar width
            val layoutParams = holder.itemAmountLeftIndicator.layoutParams
            layoutParams.width = item.getBarWidth(maxBarWidth)
            holder.itemAmountLeftIndicator.layoutParams = layoutParams

            // Set bar color
            holder.itemAmountLeftIndicator.setCardBackgroundColor(item.getBarColor(holder.itemAmountLeftIndicator.context))
        }

        // Set item image in XML layout
        val context = holder.itemView.context
        val imageName = item.imageUrl

        if (imageName.isNotEmpty()) {
            // Use GuiUtils to get the drawable resource ID
            val imageResId = GuiUtils.getDrawableResId(context, imageName)

            if (imageResId != 0) {
                holder.itemImage.setImageResource(imageResId)
            } else {
                // Use a fallback image if no matching drawable is found
                holder.itemImage.setImageResource(R.drawable.uncategorized)
            }
        } else {
            // Use a fallback image if `imageUrl` is null or empty
            holder.itemImage.setImageResource(R.drawable.uncategorized)
        }

        // Get the category color from CategoryRepository
        val category = CategoryRepository.getCategories().find { it.category_name == item.category }
        val categoryColor = if (category != null) {
            Color.parseColor(category.category_color) // Use category's color
        } else {
            Color.parseColor("#FFFFFF") // Fallback to default color
        }

        // Set card background color
        holder.itemCard.setCardBackgroundColor(categoryColor)

    }

    override fun getItemCount() = items.size
}
