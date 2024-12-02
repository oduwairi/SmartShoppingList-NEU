package com.iriawud.smartshoppinglist.ui.shopping

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.network.RecommendationItem
import com.iriawud.smartshoppinglist.ui.CategoryRepository
import com.iriawud.smartshoppinglist.ui.GuiUtils
import com.iriawud.smartshoppinglist.ui.inventory.InventoryViewModel
import com.iriawud.smartshoppinglist.ui.shopping.Item.Companion.getCurrentTimestamp

class ShoppingAdapter(
    private var items: List<Item>,
    private val shoppingViewModel: ShoppingViewModel,
    private val inventoryViewModel: InventoryViewModel,
    private val context: Context,
    private var recommendations: List<RecommendationItem> // Added recommendation data
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var groupedItems: MutableMap<String, MutableList<Item>> = items.groupBy { it.category }
        .mapValues { it.value.toMutableList() }.toMutableMap()

    companion object {
        private const val VIEW_TYPE_CATEGORY = 0
        private const val VIEW_TYPE_FOOTER = 1
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.categorySplitterText)
        val categoryItemsRecyclerView: RecyclerView = itemView.findViewById(R.id.shoppingListRecyclerView)
        val categoryImage: ImageView? = itemView.findViewById(R.id.categoryImage)
    }

    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recommendationRecyclerView: RecyclerView = itemView.findViewById(R.id.recommendationRecyclerView)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < groupedItems.size) VIEW_TYPE_CATEGORY else VIEW_TYPE_FOOTER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_CATEGORY) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shopping_category_line, parent, false)
            CategoryViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recommendation_box, parent, false)
            FooterViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CategoryViewHolder) {
            val category = groupedItems.keys.elementAt(position)
            val itemsInCategory = groupedItems[category] ?: mutableListOf()

            holder.categoryNameTextView.text = category

            val drawableResId = GuiUtils.getDrawableResId(holder.itemView.context, category)
            if (drawableResId != 0) {
                holder.categoryImage?.setImageResource(drawableResId)
            } else {
                holder.categoryImage?.setImageResource(R.drawable.ic_launcher_background)
            }

            val itemAdapter = ShoppingItemAdapter(itemsInCategory, shoppingViewModel)
            holder.categoryItemsRecyclerView.apply {
                layoutManager = LinearLayoutManager(holder.itemView.context)
                adapter = itemAdapter
            }

            // Set up swipe functionality
            val swipeHandler = ShoppingCardSwiper(
                context = context,
                adapter = itemAdapter,
                onItemDeleted = { item ->
                    itemsInCategory.remove(item)
                    itemAdapter.updateItems(itemsInCategory)
                    shoppingViewModel.deleteItem(item)

                    if (itemsInCategory.isEmpty()) {
                        removeCategoryAtPosition(position)
                    }
                },
                onItemDone = { item ->
                    itemsInCategory.remove(item)
                    itemAdapter.updateItems(itemsInCategory)
                    shoppingViewModel.deleteItem(item)
                    inventoryViewModel.addItem(item)

                    if (itemsInCategory.isEmpty()) {
                        removeCategoryAtPosition(position)
                    }
                }
            )
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(holder.categoryItemsRecyclerView)

        } else if (holder is FooterViewHolder) {
            val maxVisibleItems = 50 // Set the maximum number of visible items
            val limitedRecommendations = if (recommendations.size > maxVisibleItems) {
                recommendations.subList(0, maxVisibleItems) // Limit the recommendations
            } else {
                recommendations
            }

            val recommendationAdapter = RecommendationAdapter(limitedRecommendations, maxVisibleItems) { recommendation ->
                // Handle adding recommendation to shopping list
                val categoryMap = CategoryRepository.getCategories().associateBy { it.category_id }
                val newItem = Item(
                    id = recommendation.item_id,
                    name = recommendation.item_name,
                    quantity = "${recommendation.quantity} ${recommendation.quantity_unit ?: "pcs"}",
                    category = categoryMap[recommendation.category_id]?.category_name ?: "Uncategorized", // Map ID to name
                    price = "${recommendation.price ?: 0.0} ${recommendation.currency ?: "USD"}",
                    priority = recommendation.priority,
                    imageUrl = recommendation.image_url,
                    frequency = "${recommendation.frequency_value ?: "Not set"} ${recommendation.frequency_unit ?: ""}",
                    createdAt = getCurrentTimestamp()
                )
                shoppingViewModel.addItem(newItem)
                // Remove the recommendation via the ViewModel
                shoppingViewModel.deleteRecommendationItem(recommendation)
            }

            holder.recommendationRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = recommendationAdapter
            }
            recommendationAdapter.notifyDataSetChanged() // Ensure recommendations update
        }
    }


    override fun getItemCount(): Int = groupedItems.size + 1 // Include footer

    fun updateItems(newItems: List<Item>) {
        items = newItems
        groupedItems = items.groupBy { it.category }.mapValues { it.value.toMutableList() }.toMutableMap()
        notifyDataSetChanged()
    }

    fun updateRecommendations(newRecommendations: List<RecommendationItem>) {
        recommendations = newRecommendations
        notifyItemChanged(groupedItems.size) // Notify footer (recommendations) update
    }


    private fun removeCategoryAtPosition(position: Int) {
        val category = groupedItems.keys.elementAt(position)
        groupedItems.remove(category)
        notifyItemRemoved(position)
    }
}
