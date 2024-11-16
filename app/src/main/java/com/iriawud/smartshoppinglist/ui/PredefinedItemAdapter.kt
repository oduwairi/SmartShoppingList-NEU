package com.iriawud.smartshoppinglist.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ArrayAdapter
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.network.PredefinedItem

class PredefinedItemAdapter(
    context: Context,
    private val items: List<PredefinedItem>
) : ArrayAdapter<PredefinedItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_predefined, parent, false)

        val icon = view.findViewById<ImageView>(R.id.suggestedItemImage)
        val name = view.findViewById<TextView>(R.id.suggestedItemText)
        val category = view.findViewById<TextView>(R.id.suggestedItemCategoryText)

        item?.let {
            //set suggestion text
            name.text = it.item_name

            val imageResId = GuiUtils.getDrawableResId(context, it.image_url)
            if (imageResId != 0) {
                icon.setImageResource(imageResId)
            } else {
                // Use a fallback image if no matching drawable is found
                icon.setImageResource(R.drawable.uncategorized)
            }

            // Fetch category name from CategoryRepository
            val categoryMap = CategoryRepository.getCategories().associateBy({ it.category_id }, { it.category_name })
            val categoryName = categoryMap[it.category_id] ?: "Uncategorized"
            category.text = categoryName
        }

        return view
    }

    override fun getFilter(): android.widget.Filter {
        return object : android.widget.Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredItems = if (constraint.isNullOrEmpty()) {
                    items
                } else {
                    items.filter {
                        it.item_name.contains(constraint, ignoreCase = true)
                    }
                }
                return FilterResults().apply {
                    values = filteredItems
                    count = filteredItems.size
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                clear()
                addAll(results?.values as? List<PredefinedItem> ?: emptyList())
                notifyDataSetChanged()
            }
        }
    }

}
