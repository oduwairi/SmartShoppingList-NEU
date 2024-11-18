package com.iriawud.smartshoppinglist.ui.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.ui.GuiUtils
import com.iriawud.smartshoppinglist.ui.shopping.ShoppingItem

class InventoryAdapter(private var items: List<ShoppingItem>) :
    RecyclerView.Adapter<InventoryAdapter.CategoryViewHolder>() {

    // This variable should be mutable so it updates with each data change
    private var groupedItems: Map<String, List<ShoppingItem>> = items.groupBy { it.category }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.categoryNameText)
        val categoryItemsRecyclerView: RecyclerView = itemView.findViewById(R.id.categoryInnerRecyclerView)
        val categoryImage : ImageView = itemView.findViewById(R.id.categoryImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_category_card, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        // Get the category name and items for this position
        val category = groupedItems.keys.elementAt(position)
        val itemsInCategory = groupedItems[category] ?: emptyList()

        //set category name
        holder.categoryName.text = category

        // Set the category image
        val drawableResId = GuiUtils.getDrawableResId(holder.itemView.context, category)
        if (drawableResId != 0) {
            holder.categoryImage.setImageResource(drawableResId)
        } else {
            holder.categoryImage.setImageResource(R.drawable.ic_launcher_background) // Fallback icon
        }

        // Set up the nested RecyclerView for items in this category
        holder.categoryItemsRecyclerView.apply {
            layoutManager = GridLayoutManager(holder.itemView.context, 2)
            adapter = InventoryItemAdapter(itemsInCategory.toMutableList())
        }
    }

    override fun getItemCount() = groupedItems.size

    fun updateItems(newItems: List<ShoppingItem>) {
        // Update items and re-group by category
        items = newItems
        groupedItems = items.groupBy { it.category } // Re-group items
        notifyDataSetChanged()
    }
}
