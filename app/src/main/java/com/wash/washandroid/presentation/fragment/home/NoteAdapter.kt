import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.databinding.ItemNoteBinding
import java.util.Collections

data class Note(
    var folderId: Int,
    var title: String,
    val imageResId: Int
)

class NoteAdapter(
    val notes: MutableList<Note>,
    private val onItemClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit,
    private val onFolderNameChanged: (Note) -> Unit,
    private val onOrderChanged: (List<Note>) -> Unit, // 새로운 콜백
    private var isEditing: Boolean
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {

        private var currentNote: Note? = null
        private var textWatcher: TextWatcher? = null

        fun bind(note: Note) {
            currentNote = note
            binding.itemImageView.setImageResource(note.imageResId)
            binding.itemEditText.setText(note.title)
            binding.itemDeleteIcon.visibility = if (isEditing) View.VISIBLE else View.GONE

            // 편집 모드에 따라 EditText의 상태를 설정
            binding.itemEditText.apply {
                isFocusable = isEditing
                isClickable = isEditing
                isCursorVisible = isEditing
                isFocusableInTouchMode = isEditing
            }

            // 기존 TextWatcher가 있으면 제거
            textWatcher?.let { binding.itemEditText.removeTextChangedListener(it) }

            // 새로운 TextWatcher 추가
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (isEditing) {
                        currentNote?.let { note ->
                            note.title = s.toString()
                            onFolderNameChanged(note)
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            }
            binding.itemEditText.addTextChangedListener(textWatcher)

            // EditText가 편집 모드가 아닐 때 아이템 클릭
            binding.root.setOnClickListener {
                if (!isEditing) {
                    onItemClick(note)
                }
            }

            // 삭제 아이콘 클릭 시 삭제 처리
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

    // 편집 모드 설정
    fun setEditing(isEditing: Boolean) {
        this.isEditing = isEditing
        notifyDataSetChanged() // UI를 새로 고침
    }

    // 노트 목록 업데이트
    fun updateNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged() // UI를 새로 고침
    }

    // 아이템 이동 처리
    fun moveItem(fromPosition: Int, toPosition: Int) {
        Collections.swap(notes, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        onOrderChanged(notes) // 순서가 변경된 후 콜백 호출
    }

}
