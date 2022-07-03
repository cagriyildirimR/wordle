package com.example.wordle

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.wordle.databinding.FragmentThirdBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class ThirdFragment : Fragment() {

    private var _binding: FragmentThirdBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WordleViewModel
    private lateinit var viewModelFactory: WordleViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentThirdBinding.inflate(inflater, container, false)
        viewModelFactory = WordleViewModelFactory(binding)
        viewModel = ViewModelProvider(this, viewModelFactory).get(WordleViewModel::class.java)
        viewModel.initLetterStack()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //val wordle = viewModel.wordToList()

        viewModel.keyboardMap.forEach { (letter, button) ->
            button.setOnClickListener {
                if (!viewModel.letterStack.empty()) {
                    val current = viewModel.letterStack.pop()
                    viewModel.trash.push(current)
                    current.let { c ->
                        c.text = (it as Button).text
                    }
                }
            }
        }

        binding.deleteButton.setOnClickListener {
            if (!viewModel.trash.empty()) {
                val x = viewModel.trash.pop()
                x.text = ""
                viewModel.letterStack.push(x)
            }
        }

        lifecycleScope.launch {
            viewModel.signal.collect {
                when (it) {
                    Signal.NOTAWORD -> {
                        Toast.makeText(context, "Not in word list", Toast.LENGTH_LONG).show()
                    }
                    Signal.NEEDLETTER -> {
                        Toast.makeText(context, "Not enough letters", Toast.LENGTH_LONG).show()
                    }
                    Signal.NEXTTRY -> {
                        val (greens, yellows, greys) = viewModel.colorLogic()

                        greens.forEach { tw ->
                            tw.apply {
                                setBackgroundColor(resources.getColor(R.color.green))
                                setTextColor(Color.WHITE)
                            }
                            (viewModel.keyboardMap[tw.text.toString()] as Button).apply {
                                setBackgroundColor(resources.getColor(R.color.green))
                                setTextColor(Color.WHITE)
                            }
                        }

                        yellows.forEach { tw ->
                            tw.apply {
                                setBackgroundColor(resources.getColor(R.color.yellow))
                                setTextColor(Color.WHITE)
                            }
                            (viewModel.keyboardMap[tw.text.toString()] as Button).apply {
                                setBackgroundColor(resources.getColor(R.color.yellow))
                                setTextColor(Color.WHITE)
                            }

                        }

                        greys.forEach { tw ->
                            tw.apply {
                                setBackgroundColor(resources.getColor(R.color.dark_gray))
                                setTextColor(Color.WHITE)
                            }
                            (viewModel.keyboardMap[tw.text.toString()] as Button).apply {
                                setBackgroundColor(resources.getColor(R.color.dark_gray))
                                setTextColor(Color.WHITE)

                            }
                        }

                        viewModel.nextState()

                    }
                    Signal.GAMEOVER -> {
                        val (greens, yellows, greys) = viewModel.colorLogic()

                        greens.forEach { tw ->
                            tw.apply {
                                setBackgroundColor(resources.getColor(R.color.green))
                                setTextColor(Color.WHITE)
                            }
                            (viewModel.keyboardMap[tw.text.toString()] as Button).apply {
                                setBackgroundColor(resources.getColor(R.color.green))
                                setTextColor(Color.WHITE)
                            }
                        }

                        yellows.forEach { tw ->
                            tw.apply {
                                setBackgroundColor(resources.getColor(R.color.yellow))
                                setTextColor(Color.WHITE)
                            }
                            (viewModel.keyboardMap[tw.text.toString()] as Button).apply {
                                setBackgroundColor(resources.getColor(R.color.yellow))
                                setTextColor(Color.WHITE)
                            }

                        }

                        greys.forEach { tw ->
                            tw.apply {
                                setBackgroundColor(resources.getColor(R.color.dark_gray))
                                setTextColor(Color.WHITE)
                            }
                            (viewModel.keyboardMap[tw.text.toString()] as Button).apply {
                                setBackgroundColor(resources.getColor(R.color.dark_gray))
                                setTextColor(Color.WHITE)

                            }
                        }

                        Toast.makeText(requireContext(), viewModel.wordle, Toast.LENGTH_LONG).show()
                    }
                    Signal.WIN -> {
                        val (greens, yellows, greys) = viewModel.colorLogic()

                        greens.forEach { tw ->
                            tw.apply {
                                setBackgroundColor(resources.getColor(R.color.green))
                                setTextColor(Color.WHITE)
                            }
                            (viewModel.keyboardMap[tw.text.toString()] as Button).apply {
                                setBackgroundColor(resources.getColor(R.color.green))
                                setTextColor(Color.WHITE)
                            }
                        }
                        Toast.makeText(requireContext(), "You won", Toast.LENGTH_LONG).show()
                    }

                }
            }
        }

        binding.enterButton.setOnClickListener {
            viewModel.check()
        }

        super.onViewCreated(view, savedInstanceState)
    }


    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}