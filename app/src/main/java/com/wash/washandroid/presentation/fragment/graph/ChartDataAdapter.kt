import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R
import com.github.mikephil.charting.data.PieEntry

class ChartDataAdapter(private val chartData: List<PieEntry>, private val colors: List<Int>) :
    RecyclerView.Adapter<ChartDataAdapter.ChartDataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartDataViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chart_data, parent, false)
        return ChartDataViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChartDataViewHolder, position: Int) {
        val entry = chartData[position]
        holder.bind(entry, colors[position])
    }

    override fun getItemCount(): Int {
        return chartData.size
    }

    inner class ChartDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: ImageView = itemView.findViewById(R.id.chart_item_color)
        private val labelView: TextView = itemView.findViewById(R.id.chart_item_label)
        private val percentageView: TextView = itemView.findViewById(R.id.chart_item_percentage)

        fun bind(entry: PieEntry, color: Int) {
            labelView.text = entry.label
            percentageView.text = "${entry.value.toInt()}%"
            // 색상 적용
            colorView.setBackgroundColor(color)
            Log.d("ChartDataAdapter", "Color: $color") //여기서는 색상이 잘 나오는데,,
            //파이차트 위에만 색상이 나오지 않아 확인 필요
        }

    }
}
