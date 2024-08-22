import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.databinding.ItemChartDataBinding
import com.github.mikephil.charting.data.PieEntry

class ChartDataAdapter(
    private val chartData: List<PieEntry>,
    private val colors: List<Int>
) : RecyclerView.Adapter<ChartDataAdapter.ChartDataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartDataViewHolder {
        val binding = ItemChartDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChartDataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChartDataViewHolder, position: Int) {
        val entry = chartData[position]
        val color = colors.getOrNull(position) ?: 0 // Default to black if color is not available
        holder.bind(entry, color)
    }

    override fun getItemCount(): Int = chartData.size

    inner class ChartDataViewHolder(private val binding: ItemChartDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: PieEntry, color: Int) {
            // Apply data to views
            binding.chartItemColor.setBackgroundColor(color)
            binding.chartItemLabel.text = entry.label
            binding.chartItemPercentage.text = "${entry.value.toInt()}%"
        }
    }
}
