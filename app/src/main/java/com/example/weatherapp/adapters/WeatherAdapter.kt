package com.example.weatherapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.response.Hour
import kotlinx.android.synthetic.main.weather_rv_item.view.*


class WeatherAdapter(
    private val clickListener: (Hour) -> Unit
) :
    RecyclerView.Adapter<WeatherAdapter.WeatherAppViewHolder>() {
    private var selectedPosition = 0

    inner class WeatherAppViewHolder(itemView: View, clickAtPosition: (Int) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                clickAtPosition(absoluteAdapterPosition)
                selectedPosition = absoluteAdapterPosition

                notifyItemChanged(selectedPosition)
                notifyItemChanged(previousPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherAppViewHolder {
        val view =
            WeatherAppViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.weather_rv_item, parent, false)
            ) {
                clickListener(hourList[it])
            }
        return view
    }

    override fun onBindViewHolder(holder: WeatherAppViewHolder, position: Int) {
        val weather = hourList[position]

        val sign = if (weather.temp_c.toString().startsWith("-")) "-" else ""
        val temp = "$sign${weather.temp_c.toFloat().toInt()}°"
        val chanceOfRain = weather.chance_of_rain.toString() + "%"
        val time = weather.time.split(" ")[1]

        with(holder.itemView) {
            tvTime.text = time
            tvTemperature.text = temp
            tvRain.text = chanceOfRain
            Glide.with(this).load("https:" + weather.condition.icon).into(ivCondition)

            if (selectedPosition == position) {
                val colorSelected = ContextCompat.getColor(context, R.color.item_selected)
                rl.setBackgroundColor(colorSelected)
            } else {
                val colorNotSelected = ContextCompat.getColor(context, R.color.item_not_selected)
                rl.setBackgroundColor(colorNotSelected)
            }
        }
    }

    override fun getItemCount(): Int {
        return hourList.size
    }

    private val differCallback = object : DiffUtil.ItemCallback<Hour>() {
        override fun areItemsTheSame(oldItem: Hour, newItem: Hour): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Hour, newItem: Hour): Boolean {
            return oldItem.time == newItem.time
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    var hourList: List<Hour>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }
}