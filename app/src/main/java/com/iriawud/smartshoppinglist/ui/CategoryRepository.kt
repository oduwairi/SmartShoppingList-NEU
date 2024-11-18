package com.iriawud.smartshoppinglist.ui

import com.iriawud.smartshoppinglist.network.Category

object CategoryRepository {
    private val categoryList = mutableListOf<Category>()

    fun setCategories(categories: List<Category>) {
        categoryList.clear()
        categoryList.addAll(categories)
    }

    fun getCategories(): List<Category> {
        return categoryList
    }
}