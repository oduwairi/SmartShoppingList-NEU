package com.iriawud.smartshoppinglist.ui.shopping

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iriawud.smartshoppinglist.R
import com.iriawud.smartshoppinglist.ui.CheckoutAdapter

class CheckoutDialog(
    private val shoppingItems: List<Item>, // List of items to display
    private val onFinishCheckout: () -> Unit // Callback for the finish button
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.checkout_box, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val totalCostText = view.findViewById<TextView>(R.id.checkoutTotalCostText)
        val finishButton = view.findViewById<View>(R.id.finishCheckoutCard)
        val closeButton = view.findViewById<View>(R.id.closeCheckoutButton)
        val recyclerView = view.findViewById<RecyclerView>(R.id.checkoutItemRecyclerView)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = CheckoutAdapter(shoppingItems)

        // Calculate total cost
        val totalCost = shoppingItems.sumOf {
            val parts = it.price.split(" ")
            parts[0].toDoubleOrNull() ?: 0.0 // Extract numeric value
        }

        // Determine the unit from the first item's price
        val unit = shoppingItems.firstOrNull()?.price?.split(" ")?.getOrNull(1) ?: ""

        // Update the total cost text
        totalCostText.text = "Total: ${"%.2f".format(totalCost)} $unit"

        // Handle finish checkout button
        finishButton.setOnClickListener {
            onFinishCheckout()
            dismiss()
        }

        // Handle close button
        closeButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        // Set dialog to full-screen
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}
