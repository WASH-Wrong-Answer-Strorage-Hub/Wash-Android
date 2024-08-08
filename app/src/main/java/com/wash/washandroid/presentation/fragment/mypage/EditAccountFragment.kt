package com.wash.washandroid.presentation.fragment.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentEditAccountBinding

class EditAccountFragment: Fragment() {

    private var _binding: FragmentEditAccountBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditAccountBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editAccountBackBtn.setOnClickListener {
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, MypageFragment())
            fragmentTransaction.addToBackStack(null)
            Toast.makeText(requireContext(), "되돌아가시겠습니까?", Toast.LENGTH_SHORT).show()
            fragmentTransaction.commit()
        }

        binding.editAccountNicknameBtn.setOnClickListener {
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, EditNicknameFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }
}