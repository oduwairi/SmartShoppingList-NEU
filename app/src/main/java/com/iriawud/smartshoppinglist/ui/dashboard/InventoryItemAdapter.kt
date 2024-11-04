package com.iriawud.smartshoppinglist.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.ui.home.ShoppingItem

class InventoryItemAdapter(private val items: List<ShoppingItem>) :
    RecyclerView.Adapter<InventoryItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.inventoryItemName)
        val itemImage : ImageView = itemView.findViewById(R.id.inventoryItemImage)
        val itemAddedDate :TextView = itemView.findViewById(R.id.inventoryDateAddedText)
        val itemAmountLeft : TextView = itemView.findViewById(R.id.inventoryAmountLeftText)
        val itemTimeLeft : TextView = itemView.findViewById(R.id.inventoryTimeLeftText)
        val itemAmountLeftIndicator : CardView = itemView.findViewById(R.id.amountLeftIndicator)
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

        //set item image in xml layout
        val context = holder.itemView.context
        val imageName = item.imageUrl
        if (!imageName.isNullOrEmpty()) {
            // Get the drawable resource ID from the image name
            val imageResId = context.resources.getIdentifier(imageName, "drawable", context.packageName)

            if (imageResId != 0) {
                holder.itemImage.setImageResource(imageResId)
            } else {
                // If the resource ID is 0, use a fallback image
                holder.itemImage.setImageResource(R.drawable.ic_launcher_background)
            }
        } else {
            // If `imageUrl` is null or empty, use a fallback image
            holder.itemImage.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    override fun getItemCount() = items.size
}
