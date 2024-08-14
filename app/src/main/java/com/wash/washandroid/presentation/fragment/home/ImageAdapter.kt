import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.databinding.FragmentHomeDetailRecyeImgBinding

class ImageAdapter(
    private val itemCount: Int,
    private val onItemClick: (Int) -> Unit,
    private val itemWidth: Int,
    private var isEditing: Boolean, // 편집 모드 상태
    private val onDeleteIconClick: (Int) -> Unit // 삭제 아이콘 클릭 이벤트
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: FragmentHomeDetailRecyeImgBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (!isEditing) { // 편집 모드가 아닐 때만 클릭 이벤트 처리
                    onItemClick(adapterPosition)
                }
            }

            binding.deleteIconImageView.setOnClickListener {
                if (isEditing) { // 편집 모드일 때만 삭제 아이콘 클릭 이벤트 처리
                    onDeleteIconClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        // 뷰 바인딩 객체 생성
        val binding = FragmentHomeDetailRecyeImgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        // 레이아웃 파라미터 설정
        val layoutParams = holder.binding.imageView.layoutParams as FrameLayout.LayoutParams
        layoutParams.width = itemWidth
        layoutParams.height = itemWidth

        // 마진 설정
        val margin = 2 // 원하는 마진 값 (단위는 dp)
        val density = holder.itemView.resources.displayMetrics.density
        val marginPx = (margin * density).toInt()
        layoutParams.setMargins(marginPx, marginPx, marginPx, marginPx)
        holder.binding.imageView.layoutParams = layoutParams

        // scaleType 설정
        holder.binding.imageView.scaleType = ImageView.ScaleType.CENTER_CROP // 또는 fitCenter

        if (isEditing) {
            holder.binding.deleteIconImageView.visibility = View.VISIBLE
            holder.binding.imageView.isEnabled = false // 이미지 뷰 비활성화
        } else {
            holder.binding.deleteIconImageView.visibility = View.GONE
            holder.binding.imageView.isEnabled = true // 이미지 뷰 활성화
        }
    }

    override fun getItemCount(): Int {
        return itemCount
    }

    fun setEditing(isEditing: Boolean) {
        this.isEditing = isEditing
        notifyDataSetChanged() // 데이터 변경 알림으로 UI 업데이트
    }
}
