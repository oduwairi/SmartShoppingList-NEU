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
    import com.iriawud.smartshoppinglist.ui.GuiUtils
    import com.iriawud.smartshoppinglist.ui.inventory.InventoryViewModel

    class ShoppingAdapter(
        private var items: List<Item>,
        private val shoppingViewModel: ShoppingViewModel,
        private val inventoryViewModel: InventoryViewModel,
        private val context: Context
    ) : RecyclerView.Adapter<ShoppingAdapter.CategoryViewHolder>() {

        private var groupedItems: MutableMap<String, MutableList<Item>> = items.groupBy { it.category }.mapValues { it.value.toMutableList() }.toMutableMap()

        inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val categoryNameTextView: TextView = itemView.findViewById(R.id.categorySplitterText)
            val categoryItemsRecyclerView: RecyclerView = itemView.findViewById(R.id.shoppingListRecyclerView)
            val categoryImage: ImageView? = itemView.findViewById(R.id.categoryImage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shopping_category_line, parent, false)
            return CategoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
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
        }

        override fun getItemCount(): Int = groupedItems.size

        fun updateItems(newItems: List<Item>) {
            items = newItems
            groupedItems = items.groupBy { it.category }.mapValues { it.value.toMutableList() }.toMutableMap()
            notifyDataSetChanged()
        }

        private fun removeCategoryAtPosition(position: Int) {
            val category = groupedItems.keys.elementAt(position)
            groupedItems.remove(category)
            notifyItemRemoved(position)
        }
    }
