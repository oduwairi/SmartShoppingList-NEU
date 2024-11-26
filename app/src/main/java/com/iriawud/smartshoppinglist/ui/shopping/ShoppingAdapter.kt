package com.iriawud.smartshoppinglist.ui.shopping

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.ui.GuiUtils
import com.iriawud.smartshoppinglist.ui.GuiUtils.collapseView
import com.iriawud.smartshoppinglist.ui.GuiUtils.expandView
import com.iriawud.smartshoppinglist.ui.inventory.MathUtils

class ShoppingAdapter(
    private var items: MutableList<Item>,
    private val viewModel: ShoppingViewModel
) : RecyclerView.Adapter<ShoppingAdapter.ViewHolder>() {

    private val expandedStates = mutableMapOf<Int, Boolean>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.tvItemName)
        val itemImage : ImageView = itemView.findViewById(R.id.itemImage)
        val itemQuantity: TextView = itemView.findViewById(R.id.tvItemQuantity)
        val itemCategory : TextView = itemView.findViewById(R.id.tvItemCategory)
        val itemPriorityIndicator : CardView = itemView.findViewById(R.id.priorityColoredCard)
        val itemCost : TextView = itemView.findViewById(R.id.tvItemCostNum)
        val quantityEditText: EditText = itemView.findViewById(R.id.quantityEditText)
        val quantityUnitDropdown: Spinner = itemView.findViewById(R.id.quantityUnitSpinner)
        val prioritySlider: Slider = itemView.findViewById(R.id.prioritySlider)
        val priorityDropdown: Spinner = itemView.findViewById(R.id.prioritySpinner)
        val costEditText: EditText = itemView.findViewById(R.id.costEditText)
        val costUnitEditText: EditText = itemView.findViewById(R.id.costUnitEditText)
        val inStockBar: CardView = itemView.findViewById(R.id.amountBarStock)
        val expandableLayout: ConstraintLayout = itemView.findViewById(R.id.expandableLayout)
        val saveChangesButton: CardView = itemView.findViewById(R.id.saveChangesButton)
        val discardChangesButton: CardView = itemView.findViewById(R.id.discardChangesButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.name
        holder.itemCategory.text = item.category
        holder.itemQuantity.text = item.quantity
        holder.itemCost.text = item.price
        holder.itemPriorityIndicator.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, PriorityColor.from(item.priority).colorRes))
        holder.inStockBar.post {
            val maxBarWidth = MathUtils.dpToPx(45f, holder.inStockBar.context)

            // Calculate and set bar width
            val layoutParams = holder.inStockBar.layoutParams
            layoutParams.width = item.getBarWidth(maxBarWidth)
            holder.inStockBar.layoutParams = layoutParams

            // Set bar color
            holder.inStockBar.setCardBackgroundColor(item.getBarColor(holder.inStockBar.context))
        }

        // Set up data bindings for quantity, cost, and priority
        GuiUtils.updateQuantity(
            quantityEditText = holder.quantityEditText,
            quantityUnitDropdown = holder.quantityUnitDropdown,
            item = item,
            viewModel = viewModel,
            saveButton = holder.saveChangesButton,
            discardButton = holder.discardChangesButton
        ) {
            toggleItemCardExpansion(holder, position) // Pass the toggle function
        }
        GuiUtils.updateCost(holder.costEditText, holder.costUnitEditText, item)
        GuiUtils.updatePriority(holder.priorityDropdown, holder.prioritySlider, item)

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

        holder.itemView.setOnClickListener {
            toggleItemCardExpansion(holder, position)
        }
    }

    private fun toggleItemCardExpansion(holder: ViewHolder, position: Int) {
        val shouldExpand = !(expandedStates[position] ?: false)
        expandedStates[position] = shouldExpand

        if (shouldExpand) {
            expandView(holder.expandableLayout)
        } else {
            collapseView(holder.expandableLayout)
        }
    }


    override fun getItemCount() = items.size

    fun getItemAtPosition(position: Int): Item {
        return items[position]
    }

    fun updateItems(newItems: MutableList<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}


