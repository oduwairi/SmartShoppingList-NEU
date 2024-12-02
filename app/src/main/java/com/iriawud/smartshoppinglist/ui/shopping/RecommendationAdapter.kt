package com.iriawud.smartshoppinglist.ui.shopping

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.network.RecommendationItem
import com.iriawud.smartshoppinglist.ui.GuiUtils

class RecommendationAdapter(
    private val allRecommendations: List<RecommendationItem>, // Full list of recommendations
    private val maxVisibleItems: Int, // Maximum items to show
    private val onAddButtonClick: (RecommendationItem) -> Unit // Callback for the add button
) : RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {

    // Current items to display, limited by maxVisibleItems
    private val limitedRecommendations: List<RecommendationItem>
        get() = if (allRecommendations.size > maxVisibleItems) {
            allRecommendations.subList(0, maxVisibleItems)
        } else {
            allRecommendations
        }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.recommendationItemName)
        val itemBasedOn: TextView = itemView.findViewById(R.id.recommendationItemBasedOn)
        val itemIcon: ImageView = itemView.findViewById(R.id.recommendationItemIcon)
        val addButton: ImageView = itemView.findViewById(R.id.recommendationAddButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recommendation_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recommendation = limitedRecommendations[position]
        holder.itemName.text = recommendation.item_name
        holder.itemBasedOn.text = recommendation.recommendation_msg
        // Set item image in XML layout
        GuiUtils.setDrawable(holder.itemView.context, holder.itemIcon, recommendation.image_url)

        // Set click listener for the add button
        holder.addButton.setOnClickListener {
            onAddButtonClick(recommendation)
        }
    }

    override fun getItemCount(): Int = limitedRecommendations.size
}

