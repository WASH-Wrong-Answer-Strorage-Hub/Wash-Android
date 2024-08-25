import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.databinding.ItemNoteBinding

data class Note(
    var folderId : Int,
    var title: String,
    val imageResId: Int
)

class NoteAdapter(
    private val notes: MutableList<Note>,  // note 리스트를 외부에서 접근할 수 있도록 val로 변경
    private val onItemClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit,
    private val onFolderNameChanged: (Note) -> Unit,  // 폴더 이름 변경 콜백 추가
    private var isEditing: Boolean
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {

        private var currentTextWatcher: TextWatcher? = null

        fun bind(note: Note) {
            // 기존의 TextWatcher를 제거합니다.
            currentTextWatcher?.let {
                binding.itemEditText.removeTextChangedListener(it)
            }

            // EditText에 현재 Note의 title을 설정합니다.
            binding.itemEditText.setText(note.title)

            // 새로운 TextWatcher를 추가합니다.
            currentTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (isEditing) {
                        note.title = s.toString()
                        onFolderNameChanged(note)  // 변경된 이름을 ViewModel로 전달
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            }

            binding.itemEditText.addTextChangedListener(currentTextWatcher)

            // 편집 모드에 따라 EditText의 상태를 변경
            binding.itemEditText.apply {
                isFocusable = isEditing
                isClickable = isEditing
                isCursorVisible = isEditing
                isFocusableInTouchMode = isEditing
            }

            binding.root.setOnClickListener {
                if (!isEditing) {
                    onItemClick(note)
                }
            }

            binding.itemDeleteIcon.setOnClickListener {
                onDeleteClick(note)
            }

            binding.itemDeleteIcon.visibility = if (isEditing) View.VISIBLE else View.GONE
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
        notifyDataSetChanged()  // UI를 업데이트
    }

    // 노트를 업데이트하는 함수
    fun updateNotes(newNotes: List<Note>) {
        notes.clear()  // 기존 데이터 삭제
        notes.addAll(newNotes)  // 새 데이터 추가
        notifyDataSetChanged()  // UI 갱신
    }
}
