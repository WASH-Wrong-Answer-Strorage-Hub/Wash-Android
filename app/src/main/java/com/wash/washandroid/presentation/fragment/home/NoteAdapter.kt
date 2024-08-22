import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.databinding.ItemNoteBinding

data class Note(
    var title: String, // 제목을 수정 가능하도록 var로 변경
    val imageResId: Int
)

class NoteAdapter(
    private val notes: List<Note>,
    private val onItemClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit,
    private var isEditing: Boolean
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {

        // EditText에 TextWatcher 추가
        fun bind(note: Note) {
            binding.itemImageView.setImageResource(note.imageResId)
            binding.itemEditText.setText(note.title) // EditText에 제목 설정
            binding.itemDeleteIcon.visibility = if (isEditing) View.VISIBLE else View.GONE

            // 편집 모드에 따라 EditText의 상태를 변경
            binding.itemEditText.apply {
                isFocusable = isEditing
                isClickable = isEditing
                isCursorVisible = isEditing
                isFocusableInTouchMode = isEditing
            }

            // EditText의 텍스트 변경 감지
            binding.itemEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 사용하지 않음
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 편집 중인 텍스트를 Note 객체에 반영
                    if (isEditing) {
                        note.title = s.toString()  // Note 객체의 title 업데이트
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // 사용하지 않음
                }
            })

            binding.root.setOnClickListener {
                if (!isEditing) {
                    onItemClick(note)
                }
            }

            binding.itemDeleteIcon.setOnClickListener {
                onDeleteClick(note)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    // 편집 모드 상태를 설정하는 함수
    fun setEditing(isEditing: Boolean) {
        this.isEditing = isEditing
        notifyDataSetChanged() // UI를 업데이트
    }
}
