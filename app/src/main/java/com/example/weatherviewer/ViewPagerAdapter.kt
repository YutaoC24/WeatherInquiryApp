package com.example.weatherviewer

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private var details = emptyArray<String>()
    private var temps = ArrayList<String>()

    fun setDetails(detail: Array<String>) {
        details = detail
    }

    fun setTemps(temps_in: ArrayList<String>) {
        temps = temps_in
    }

    override fun getItemCount(): Int {
        return 3  // Number of tabs
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Tab1Fragment.newInstance(details)
            1 -> Tab2Fragment.newInstance(temps)
            2 -> Tab3Fragment.newInstance(details)
            else -> Tab1Fragment.newInstance(emptyArray())
        }
    }
}