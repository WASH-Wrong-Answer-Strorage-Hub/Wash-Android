import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R

class ImageAdapter(
    private val itemCount: Int,
    private val onItemClick: (Int) -> Unit,
    private val itemWidth: Int,
    private var isEditing: Boolean, // 편집 모드 상태
    private val onDeleteIconClick: (Int) -> Unit // 삭제 아이콘 클릭 이벤트
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val deleteIconImageView: ImageView = itemView.findViewById(R.id.deleteIconImageView)

        init {
            itemView.setOnClickListener {
                if (!isEditing) { // 편집 모드가 아닐 때만 클릭 이벤트 처리
                    onItemClick(adapterPosition)
                }
            }

            deleteIconImageView.setOnClickListener {
                if (isEditing) { // 편집 모드일 때만 삭제 아이콘 클릭 이벤트 처리
                    onDeleteIconClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_home_detail_recye_img, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        // 레이아웃 파라미터 설정
        val layoutParams = holder.imageView.layoutParams as FrameLayout.LayoutParams
        layoutParams.width = itemWidth
        layoutParams.height = itemWidth

        // 마진 설정
        val margin = 2 // 원하는 마진 값 (단위는 dp)
        val density = holder.itemView.resources.displayMetrics.density
        val marginPx = (margin * density).toInt()
        layoutParams.setMargins(marginPx, marginPx, marginPx, marginPx)
        holder.imageView.layoutParams = layoutParams

        // scaleType 설정
        holder.imageView.scaleType = ImageView.ScaleType.CENTER_CROP // 또는 fitCenter

        if (isEditing) {
            holder.deleteIconImageView.visibility = View.VISIBLE
            holder.imageView.isEnabled = false // 이미지 뷰 비활성화
        } else {
            holder.deleteIconImageView.visibility = View.GONE
            holder.imageView.isEnabled = true // 이미지 뷰 활성화
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
