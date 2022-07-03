package com.example.wordle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wordle.databinding.FragmentThirdBinding

class WordleViewModelFactory(private val binding: FragmentThirdBinding): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordleViewModel::class.java)) {
            return WordleViewModel(binding) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}