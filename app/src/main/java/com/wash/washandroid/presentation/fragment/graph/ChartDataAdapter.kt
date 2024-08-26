import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.databinding.ItemChartDataBinding
import com.wash.washandroid.presentation.fragment.graph.ChartItem

class ChartDataAdapter(
    private val chartData: List<ChartItem> // ChartItem 리스트 사용
) : RecyclerView.Adapter<ChartDataAdapter.ChartDataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartDataViewHolder {
        val binding = ItemChartDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChartDataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChartDataViewHolder, position: Int) {
        val item = chartData[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = chartData.size

    inner class ChartDataViewHolder(private val binding: ItemChartDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChartItem) {
            // Apply data to views
            binding.chartItemColor.setBackgroundColor(item.color)
            binding.chartItemLabel.text = item.category
            binding.chartItemPercentage.text = item.percentage
        }
    }
}
