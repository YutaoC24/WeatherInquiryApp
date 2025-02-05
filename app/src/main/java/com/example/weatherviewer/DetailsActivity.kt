package com.example.weatherviewer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.weatherviewer.databinding.DetailPageBinding
import com.example.weatherviewer.databinding.MainLayoutBinding
import com.example.weatherviewer.ui.theme.WeatherViewerTheme
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: DetailPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailPageBinding.inflate(layoutInflater)
        val view = binding.root
        setTheme(R.style.Theme_WeatherViewer)

        // get the passed arguments from the previous activity
        val city = intent.getStringExtra("city")
        val state = intent.getStringExtra("state")
        val details = intent.getStringArrayExtra("details")
        val temps = intent.getStringArrayListExtra("temps")
        var temp = "N/A"

        if (details != null) {
            for (i in details) {
                Log.i("result", "$i")
            }
            temp = details[3]
        }

        // set the city and state in detail page
        binding.stateAndCity.setText("$city, $state")
        binding.backButton2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.tweetButton.setOnClickListener {
            val tweetMessage = "Check out $city, $state's weather! It is $temp°F!&hashtags=CSCI571WeatherSearch"
            val tweetUrl = "https://twitter.com/intent/tweet?text=$tweetMessage"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl))
            startActivity(intent)
        }

        val viewPager: ViewPager2 = binding.viewPager
        val tabLayout: TabLayout = binding.tabLayout

        val adapter = ViewPagerAdapter(this)
        if (details != null) {
            adapter.setDetails(details)
        }
        if (temps != null) {
            adapter.setTemps(temps)
        }
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "TODAY"
                1 -> "WEEKLY"
                else -> "WEATHER DATA"
            }
            tab.icon = when (position) {
                0 -> ContextCompat.getDrawable(this, R.drawable.today)
                1 -> ContextCompat.getDrawable(this, R.drawable.weekly_tab)
                else -> ContextCompat.getDrawable(this, R.drawable.weather_data_tab)
            }
        }.attach()

        /*
        // Set up the adapter for ViewPager2
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        // Connect TabLayout and ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->

            tab.text = when (position) {
                0 -> "TODAY"
                1 -> "WEEKLY"
                else -> "WEATHER DATA"
            }
            tab.icon = when (position) {
                0 -> ContextCompat.getDrawable(this, R.drawable.today)
                1 -> ContextCompat.getDrawable(this, R.drawable.weekly_tab)
                else -> ContextCompat.getDrawable(this, R.drawable.weather_data_tab)
            }
        }.attach()
        */

        setContentView(view)
    }
}