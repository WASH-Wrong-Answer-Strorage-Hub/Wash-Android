package com.wash.washandroid.presentation.fragment.note

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class QuestionModePagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = listOf(
        SingleQuestionFragment(),
        MultipleQuestionsFragment()
    )

    private val fragmentTitleList = listOf(
        "한 문제",
        "여러 문제"
    )

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitleList[position]
    }
}

