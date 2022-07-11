package com.example.wordle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.wordle.databinding.FragmentGameScreenBinding
import com.example.wordle.util.flipListOfTextViews
import com.example.wordle.util.shakeAnimation
import com.example.wordle.util.slightlyScaleUpAnimation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class GameScreenFragment : Fragment() {

    private var _binding: FragmentGameScreenBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<WordleViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentGameScreenBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val listOfTextViews = listOf(
            listOf(
                binding.firstRow1,
                binding.firstRow2,
                binding.firstRow3,
                binding.firstRow4,
                binding.firstRow5
            ),
            listOf(
                binding.secondRow1,
                binding.secondRow2,
                binding.secondRow3,
                binding.secondRow4,
                binding.secondRow5
            ),
            listOf(
                binding.thirdRow1,
                binding.thirdRow2,
                binding.thirdRow3,
                binding.thirdRow4,
                binding.thirdRow5
            ),
            listOf(
                binding.fourthRow1,
                binding.fourthRow2,
                binding.fourthRow3,
                binding.fourthRow4,
                binding.fourthRow5
            ),
            listOf(
                binding.fifthRow1,
                binding.fifthRow2,
                binding.fifthRow3,
                binding.fifthRow4,
                binding.fifthRow5
            ),
            listOf(
                binding.sixthRow1,
                binding.sixthRow2,
                binding.sixthRow3,
                binding.sixthRow4,
                binding.sixthRow5
            )
        )

        val lettersRow = listOf(
            binding.firstLettersRow,
            binding.secondLettersRow,
            binding.thirdLettersRow,
            binding.fourthLettersRow,
            binding.fifthLettersRow,
            binding.sixthLettersRow
        )
        val keyboardMap = mapOf<String, Button>(
            "A" to binding.A,
            "B" to binding.B,
            "C" to binding.C,
            "D" to binding.D,
            "E" to binding.E,
            "F" to binding.F,
            "G" to binding.G,
            "H" to binding.H,
            "I" to binding.I,
            "J" to binding.J,
            "K" to binding.K,
            "L" to binding.L,
            "M" to binding.M,
            "N" to binding.N,
            "O" to binding.O,
            "P" to binding.P,
            "Q" to binding.Q,
            "R" to binding.RR,
            "S" to binding.S,
            "T" to binding.T,
            "U" to binding.U,
            "V" to binding.V,
            "W" to binding.W,
            "X" to binding.X,
            "Y" to binding.Y,
            "Z" to binding.Z,
        )

        var shakeAnimation = shakeAnimation(lettersRow[viewModel.currentPosition.row])

        keyboardMap.forEach { (letter, button) ->
            lifecycleScope.launch{
                viewModel.listOfKeys[letter]!!.collect { key ->
                    button.apply {
                        setBackgroundColor(resources.getColor(key.backgroundColor))
                        setTextColor(resources.getColor(key.textColor))
                    }
                }
            }
            button.setOnClickListener {
                viewModel.setLetter(letter)
            }
        }

        listOfTextViews.forEachIndexed { rows, list ->
            list.forEachIndexed { cols, textView ->
                lifecycleScope.launch {
                    viewModel.listOfTextViews[rows][cols].collect { s ->
                        if (s.letter != " " && s.backgroundColor == R.color.white) {
                            slightlyScaleUpAnimation(textView)
                        }
                        textView.apply {
                            text = s.letter
                            background = resources.getDrawable(s.backgroundColor)
                            setTextColor(resources.getColor(s.textColor))
                        }
                    }
                }
            }
        }

        binding.deleteButton.setOnClickListener {
            viewModel.deleteLetter()
        }

        binding.enterButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.checkRow()
            }
        }

        lifecycleScope.launch {
            viewModel.signal.collect {
                when (it) {
                    Signal.NOTAWORD -> {
                        Toast.makeText(context, "Not in word list", Toast.LENGTH_LONG).show()
                        shakeAnimation()
                    }
                    Signal.NEEDLETTER -> {
                        shakeAnimation()
                        Toast.makeText(context, "Not enough letters", Toast.LENGTH_LONG).show()
                    }
                    Signal.NEXTTRY -> {
                        flip(
                            listOfTextViews[viewModel.currentPosition.row],
                            viewModel.checkColor(),
                        ) {
                            viewModel.emitColor()
                            viewModel.currentPosition.nextRow()
                            shakeAnimation = shakeAnimation(lettersRow[viewModel.currentPosition.row])
                        }
                    }
                    Signal.GAMEOVER -> {

                        flip(
                            listOfTextViews[viewModel.currentPosition.row],
                            viewModel.checkColor(),
                        ) {
                            viewModel.emitColor()
                            viewModel.resetGame()
                        }

                        Toast.makeText(context, "Gameover: ${viewModel.wordle}", Toast.LENGTH_LONG)
                            .show()
                    }
                    Signal.WIN -> {
                        flip(
                            listOfTextViews[viewModel.currentPosition.row],
                            viewModel.checkColor(),
                        ) {
                            viewModel.emitColor()
                            viewModel.resetGame()
                        }
                        Toast.makeText(context, "YouWon", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun flip(
        listOfTextViews: List<TextView>,
        letters: List<Letter>,
        reset: Boolean = false,
        doOnEnd: () -> Unit
    ) {
        flipListOfTextViews(
            listOfTextViews,
            letters,
            reset = reset
        ) {
            doOnEnd()
        }.start()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
