package com.wash.washandroid.presentation.fragment.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R
import com.wash.washandroid.databinding.ItemChartDataBinding

class ChartDataAdapter(
    private val chartData: List<ChartItem>
) : RecyclerView.Adapter<ChartDataAdapter.ChartDataViewHolder>() {

    private val colors = listOf(
        R.color.main,
        R.color.sub2,
        R.color.sub3,
        R.color.sub4,
        R.color.sub5
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartDataViewHolder {
        val binding = ItemChartDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChartDataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChartDataViewHolder, position: Int) {
        val item = chartData[position]
        val color = ContextCompat.getColor(holder.itemView.context, colors[position % colors.size])
        holder.bind(item, color)
    }

    override fun getItemCount(): Int = chartData.size

    inner class ChartDataViewHolder(private val binding: ItemChartDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChartItem, color: Int) {
            binding.chartItemColor.setBackgroundColor(color)
            binding.chartItemLabel.text = item.category
            binding.chartItemPercentage.text = item.percentage
        }
    }
}