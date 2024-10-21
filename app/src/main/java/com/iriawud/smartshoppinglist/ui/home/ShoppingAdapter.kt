package com.iriawud.smartshoppinglist.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R

class ShoppingAdapter(
    private val items: List<ShoppingItem>,
    private val onItemDeleted: (ShoppingItem) -> Unit
) : RecyclerView.Adapter<ShoppingAdapter.ViewHolder>() {  // Corrected here

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.tvItemName)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        // Add more UI components if necessary
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.itemName
        holder.deleteButton.setOnClickListener { onItemDeleted(item) }
        // Bind additional item attributes to other views here
    }

    override fun getItemCount() = items.size
}
