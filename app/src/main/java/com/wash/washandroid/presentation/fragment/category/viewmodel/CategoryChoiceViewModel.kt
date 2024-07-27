package com.wash.washandroid.presentation.fragment.category.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wash.washandroid.R

open class CategoryChoiceViewModel : ViewModel() {

    private val _selectedButtonId = MutableLiveData<Int?>(null)
    val selectedButtonId: LiveData<Int?> get() = _selectedButtonId

    // TODO: 추후 디자인 변경에 따른 다음 버튼 활성화 여부 작성 예정
    private val _isNextButtonEnabled = MutableLiveData(false)
    val isNextButtonEnabled: LiveData<Boolean> = _isNextButtonEnabled

    fun onButtonClicked(buttonId: Int) {
        if (_selectedButtonId.value == buttonId) return
        _selectedButtonId.value = buttonId

        _isNextButtonEnabled.value = true
    }

    fun getButtonBackground(buttonId: Int): Int {
        return when {
            _selectedButtonId.value == buttonId && buttonId == R.id.category_add -> {
                R.drawable.category_choice_add_clicked_btn
            }
            buttonId == R.id.category_add -> {
                R.drawable.category_choice_add_btn
            }
            _selectedButtonId.value == buttonId -> {
                R.drawable.category_choice_clicked_btn
            }
            else -> {
                R.drawable.category_choice_btn
            }
        }
    }
}