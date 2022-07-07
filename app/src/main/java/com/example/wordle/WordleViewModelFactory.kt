package com.example.wordle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wordle.databinding.FragmentGameScreenBinding

class WordleViewModelFactory(private val binding: FragmentGameScreenBinding): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordleViewModel::class.java)) {
            return WordleViewModel(binding) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}