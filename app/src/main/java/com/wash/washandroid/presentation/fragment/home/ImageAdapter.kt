import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wash.washandroid.databinding.FragmentHomeDetailRecyeImgBinding
import com.wash.washandroid.presentation.fragment.home.Problem

class ImageAdapter(
    var items: List<Problem>, // 초기 데이터로 빈 리스트를 사용
    private val onItemClick: (Int) -> Unit,
    private val itemWidth: Int,
    private var isEditing: Boolean,
    private val onDeleteIconClick: (Int) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: FragmentHomeDetailRecyeImgBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (!isEditing) {
                    onItemClick(adapterPosition)
                }
            }
            binding.deleteIconImageView.setOnClickListener {
                if (isEditing) {
                    onDeleteIconClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = FragmentHomeDetailRecyeImgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val problem = items[position]
        Glide.with(holder.itemView.context)
            .load(problem.problemImage)
            .into(holder.binding.imageView)

        val layoutParams = holder.binding.imageView.layoutParams as FrameLayout.LayoutParams
        layoutParams.width = itemWidth
        layoutParams.height = itemWidth
        holder.binding.imageView.layoutParams = layoutParams

        if (isEditing) {
            holder.binding.deleteIconImageView.visibility = View.VISIBLE
            holder.binding.imageView.isEnabled = false
        } else {
            holder.binding.deleteIconImageView.visibility = View.GONE
            holder.binding.imageView.isEnabled = true
        }
    }

    override fun getItemCount(): Int = items.size

    fun setEditing(isEditing: Boolean) {
        this.isEditing = isEditing
        notifyDataSetChanged()
    }
}
