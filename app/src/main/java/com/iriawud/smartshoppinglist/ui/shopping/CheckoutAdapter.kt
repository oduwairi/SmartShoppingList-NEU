package com.iriawud.smartshoppinglist.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.ui.shopping.Item

class CheckoutAdapter(
    private val items: List<Item>
) : RecyclerView.Adapter<CheckoutAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.textView10)
        val itemPrice: TextView = itemView.findViewById(R.id.textView13)
        val itemQty: TextView = itemView.findViewById(R.id.checkoutItemQty)
        val itemIcon: ImageView = itemView.findViewById(R.id.checkoutItemIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.checkout_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.name
        holder.itemPrice.text = "${item.price}"
        holder.itemQty.text = item.quantity.toString()
        // Set item icon if applicable
        GuiUtils.setDrawable(holder.itemView.context, holder.itemIcon, item.imageUrl)
    }

    override fun getItemCount(): Int = items.size
}
