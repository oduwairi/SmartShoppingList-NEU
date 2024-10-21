package com.iriawud.smartshoppinglist.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.iriawud.smartshoppinglist.databinding.FragmentShoppingBinding

class ShoppingFragment : Fragment() {

private var _binding: FragmentShoppingBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val homeViewModel =
            ViewModelProvider(this).get(ShoppingViewModel::class.java)

    _binding = FragmentShoppingBinding.inflate(inflater, container, false)
    val root: View = binding.root
    return root
  }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}