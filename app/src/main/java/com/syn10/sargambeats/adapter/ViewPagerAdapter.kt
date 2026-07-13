package com.syn10.sargambeats.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.syn10.sargambeats.ui.fragment.FragmentFavourites
import com.syn10.sargambeats.ui.fragment.FragmentPlaying
import com.syn10.sargambeats.ui.fragment.FragmentPlaylist
import com.syn10.sargambeats.ui.fragment.FragmentSongs

class ViewPagerAdapter(
    activity: FragmentActivity,
    private val songIndex: Int = 0    // 👈 add
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {

            0 -> FragmentSongs()

            1 -> {
                val fragment = FragmentPlaying()
                val bundle = Bundle()
                bundle.putInt("index", songIndex)   // 👈 pass index
                fragment.arguments = bundle
                fragment
            }

            2 -> FragmentPlaylist()

            else -> FragmentFavourites()
        }
    }
}