package com.wash.washandroid.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R

data class Note(
    val title: String,
    val imageResId: Int
)

class NoteAdapter(
    private val notes: List<Note>,
    private val onItemClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit,
    private var isEditing: Boolean
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image_view)
        val textView: TextView = itemView.findViewById(R.id.item_text_view)
        val deleteIcon: ImageView = itemView.findViewById(R.id.item_delete_icon)

        fun bind(note: Note) {
            imageView.setImageResource(note.imageResId)
            textView.text = note.title
            deleteIcon.visibility = if (isEditing) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                if (!isEditing) {
                    onItemClick(note)
                }
            }

            deleteIcon.setOnClickListener {
                onDeleteClick(note)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    fun setEditing(isEditing: Boolean) {
        this.isEditing = isEditing
        notifyDataSetChanged()
    }
}
