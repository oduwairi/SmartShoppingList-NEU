package com.iriawud.smartshoppinglist.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R

class ShoppingAdapter(
    private var items: MutableList<ShoppingItem>,
    private val onItemDeleted: (ShoppingItem) -> Unit
) : RecyclerView.Adapter<ShoppingAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.tvItemName)
        val itemImage : ImageView = itemView.findViewById(R.id.itemImage)
        val itemCategory : TextView = itemView.findViewById(R.id.tvItemCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.name
        holder.itemCategory.text = item.category
        // Set the image if `imageUrl` is available
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

    fun getItemAtPosition(position: Int): ShoppingItem {
        return items[position]
    }

    fun updateItems(newItems: MutableList<ShoppingItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}


