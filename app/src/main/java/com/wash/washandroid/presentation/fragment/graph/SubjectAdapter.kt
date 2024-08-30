package com.wash.washandroid.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.databinding.ItemSubjectBinding

data class Subject(val id: Int, val name: String, val type: Int) // type을 Int로 설정

class SubjectsAdapter(
    private val subjectList: List<Subject>,
    private val onArrowClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>() {

    class SubjectViewHolder(
        private val binding: ItemSubjectBinding,
        private val onArrowClick: (Subject) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(subject: Subject) {
            // `categoryTextView`에 `name` 설정
            binding.categoryTextView.text = subject.name

            // `TypeTextView`에 `type` 설정
            binding.TypeTextView.text = "${subject.type}개의 실수" // 서브 카테고리의 개수

            // Arrow 이미지 클릭 리스너 설정
            binding.subjectArrowImageView.setOnClickListener {
                onArrowClick(subject)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SubjectViewHolder(binding, onArrowClick)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subjectList[position])
    }

    override fun getItemCount(): Int = subjectList.size
}
