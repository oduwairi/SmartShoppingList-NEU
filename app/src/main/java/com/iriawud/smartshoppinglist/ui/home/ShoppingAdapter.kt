package com.iriawud.smartshoppinglist.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R

class ShoppingAdapter(private val items: List<ShoppingItem>, private val onItemDeleted: (ShoppingItem) -> Unit) : RecyclerView.Adapter<ShoppingAdapter.ViewAdapter>() {
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping_card, parent, false)
        return ViewHolder(view)
    }
}