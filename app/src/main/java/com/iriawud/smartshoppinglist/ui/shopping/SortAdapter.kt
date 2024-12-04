package com.iriawud.smartshoppinglist.ui.shopping

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.ui.GuiUtils

class SortedItemsAdapter(var items: List<Item>) : RecyclerView.Adapter<SortedItemsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNumber: TextView = itemView.findViewById(R.id.sortNumberText)
        val itemName: TextView = itemView.findViewById(R.id.sortedItemName)
        val itemIcon: ImageView = itemView.findViewById(R.id.sortedItemIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sort_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemNumber.text = (position + 1).toString()
        holder.itemName.text = item.name
        GuiUtils.setDrawable(holder.itemView.context, holder.itemIcon, item.imageUrl)
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}
