package com.example.weatherviewer

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapterMain(fragmentActivity: MainActivity) : FragmentStateAdapter(fragmentActivity) {
    private var tabNum = 0
    private var currentInfo = ""
    private var favList = ArrayList<String>()

    fun setTabNum(num: Int) {
        tabNum = num
    }

    fun setCurrentInfo(currentInfo: String) {
        this.currentInfo = currentInfo
    }

    fun setFavInfo(favInfo: ArrayList<String>) {
        this.favList = favInfo
    }

    override fun getItemCount(): Int {
        return tabNum  // Number of tabs
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                TabCurrentFrag.newInstance(currentInfo, false)
            }
            else -> {
                TabCurrentFrag.newInstance(favList[position - 1], true)
            }
        }
    }
}