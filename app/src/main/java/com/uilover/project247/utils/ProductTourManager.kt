package com.uilover.project247.utils

import android.content.Context
import android.content.SharedPreferences

class ProductTourManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "product_tour_prefs", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_TOUR_COMPLETED = "tour_completed"
    }
    
    fun hasCompletedTour(): Boolean {
        return prefs.getBoolean(KEY_TOUR_COMPLETED, false)
    }
    
    fun setTourCompleted() {
        prefs.edit().putBoolean(KEY_TOUR_COMPLETED, true).apply()
    }
    
    fun resetTour() {
        prefs.edit().putBoolean(KEY_TOUR_COMPLETED, false).apply()
    }
}
