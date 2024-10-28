package com.iriawud.smartshoppinglist.ui.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R

class ShoppingCardSwiper(
    context: Context,
    private val adapter: ShoppingAdapter,
    private val onItemDeleted: (ShoppingItem) -> Unit,
    private val onItemDone: (ShoppingItem) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val deleteIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.delete)
    private val doneIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.tick_checkbox_svgrepo_com)
    private val iconSize = 140 // Size of the icons (in pixels)
    private val paint = Paint()

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
            ItemTouchHelper.LEFT -> {
                onItemDeleted(item)
            }
            ItemTouchHelper.RIGHT -> {
                onItemDone(item)
            }
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

        // Swiping to the left (delete)
        if (dX < 0) {
            drawIconWithGradient(
                c,
                itemView.right.toFloat() + dX,
                itemView.right.toFloat(),
                itemView.top.toFloat(),
                itemView.bottom.toFloat(),
                Color.RED,
                deleteIcon,
                itemView.right - iconMargin - iconSize,
                itemView.top + iconMargin
            )
        }

        // Swiping to the right (done)
        else if (dX > 0) {
            drawIconWithGradient(
                c,
                itemView.left.toFloat(),
                itemView.left.toFloat() + dX,
                itemView.top.toFloat(),
                itemView.bottom.toFloat(),
                Color.GREEN,
                doneIcon,
                itemView.left + iconMargin,
                itemView.top + iconMargin
            )
        }
    }

    private fun drawIconWithGradient(
        canvas: Canvas,
        startX: Float,
        endX: Float,
        top: Float,
        bottom: Float,
        color: Int,
        icon: Drawable?,
        iconLeft: Int,
        iconTop: Int
    ) {
        // Create a sharper gradient that fades out more quickly
        val gradient = LinearGradient(
            startX,
            top,
            endX,
            bottom,
            intArrayOf(color, Color.TRANSPARENT),
            floatArrayOf(0.2f, 0.8f), // Adjusted values for a sharper fade-off
            Shader.TileMode.CLAMP
        )

        paint.shader = gradient
        canvas.drawRect(startX, top, endX, bottom, paint)

        // Draw the icon resized to the defined bounds
        icon?.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
        icon?.draw(canvas)
    }
}
