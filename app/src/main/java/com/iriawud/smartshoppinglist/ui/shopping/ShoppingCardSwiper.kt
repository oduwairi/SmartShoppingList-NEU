package com.iriawud.smartshoppinglist.ui.shopping

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R

class ShoppingCardSwiper(
    private val context: Context,
    private val adapter: ShoppingAdapter,
    private val onItemDeleted: (ShoppingItem) -> Unit,
    private val onItemDone: (ShoppingItem) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val deleteIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.delete)
    private val doneIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.tick_circle_svgrepo_com)
    private val iconSize = 100 // Size of the icons (in pixels)
    private val textPaint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.black)
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val item = adapter.getItemAtPosition(position)

        when (direction) {
            ItemTouchHelper.LEFT -> onItemDeleted(item)
            ItemTouchHelper.RIGHT -> onItemDone(item)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView
        val iconMargin = (itemView.height - iconSize) / 2
        val iconTop = itemView.top + iconMargin
        val iconLeftDelete = itemView.right - iconMargin - iconSize
        val iconLeftDone = itemView.left + iconMargin
        val textY = iconTop + iconSize + 40  // Text position below the icon

        val swipeProgress = Math.abs(dX) / itemView.width.toFloat()  // Calculate the swipe progress as a fraction

        // Color interpolation based on swipe progress
        val startColorGreen = ContextCompat.getColor(context, R.color.light_green_start)
        val endColorGreen = ContextCompat.getColor(context, R.color.deep_green_end)
        val currentColorGreen = ColorUtils.blendARGB(startColorGreen, endColorGreen, swipeProgress)

        val startColorRed = ContextCompat.getColor(context, R.color.light_red_start)
        val endColorRed = ContextCompat.getColor(context, R.color.deep_red_end)
        val currentColorRed = ColorUtils.blendARGB(startColorRed, endColorRed, swipeProgress)

        // Draw the delete icon and text
        if (dX < 0) {
            deleteIcon?.setColorFilter(currentColorRed, PorterDuff.Mode.SRC_IN)
            deleteIcon?.setBounds(iconLeftDelete, iconTop, iconLeftDelete + iconSize, iconTop + iconSize)
            deleteIcon?.draw(c)
            c.drawText("Delete", iconLeftDelete + iconSize / 2f, textY.toFloat(), textPaint)
        }

        // Draw the done icon and text
        else if (dX > 0) {
            doneIcon?.setColorFilter(currentColorGreen, PorterDuff.Mode.SRC_IN)
            doneIcon?.setBounds(iconLeftDone, iconTop, iconLeftDone + iconSize, iconTop + iconSize)
            doneIcon?.draw(c)
            c.drawText("Check", iconLeftDone + iconSize / 2f, textY.toFloat(), textPaint)
        }
    }
}
