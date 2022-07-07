package com.example.wordle

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.wordle.databinding.FragmentGameScreenBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class GameScreenFragment : Fragment() {

    private var _binding: FragmentGameScreenBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WordleViewModel
    private lateinit var viewModelFactory: WordleViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentGameScreenBinding.inflate(inflater, container, false)
        viewModelFactory = WordleViewModelFactory(binding)
        viewModel = ViewModelProvider(this, viewModelFactory).get(WordleViewModel::class.java)
        viewModel.initLetterStack()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Toast.makeText(requireContext(), "word is ${viewModel.wordle}", Toast.LENGTH_LONG).show()

        viewModel.keyboardMap.forEach { (letter, button) ->
            button.setOnClickListener {
                if (!viewModel.letterStack.empty()) {
                    val current = viewModel.letterStack.pop()
                    viewModel.trash.push(current)
                    current.let { c ->
                        c.text = (it as Button).text
                        ValueAnimator.ofFloat(c.scaleX, 1.1f, 1f).apply {
                            duration = 100
                            addUpdateListener {
                                c.scaleX = animatedValue as Float
                                c.scaleY = animatedValue as Float
                            }
                            start()
                        }
                    }
                }
            }
        }

        binding.deleteButton.setOnClickListener {
            if (!viewModel.trash.empty()) {
                val x = viewModel.trash.pop()
                x.text = " "
                viewModel.letterStack.push(x)
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
                        checkRow()
                        viewModel.nextState()
                    }
                    Signal.GAMEOVER -> {
                        checkRow(::uiReset)
                        Toast.makeText(requireContext(), viewModel.wordle, Toast.LENGTH_LONG).show()
                        viewModel.reset()
                    }
                    Signal.WIN -> {
                        checkRow(::uiReset)
                        Toast.makeText(requireContext(), "You won", Toast.LENGTH_LONG).show()
                        //uiReset()
                        viewModel.reset()
                    }
                }
            }
        }

        binding.enterButton.setOnClickListener {
            viewModel.check()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun uiReset() {
        val list = mutableListOf<Animator>()
        val dur = 10L
        while (viewModel.resetStack.isNotEmpty()) {

            val x = viewModel.resetStack.pop()
            val reverseFontColorToDefaultAnimation =
                ObjectAnimator.ofArgb(x.first, "textColor", resources.getColor(R.color.black))
                    .apply {
                        duration = dur
                        doOnEnd { //
                         x.first.text = " "
                         x.first.setBackgroundResource(R.drawable.border)
                        }
                    }
            val reverseButtonBackgroundToDefaultAnimation =
                ObjectAnimator.ofArgb(x.second, "backgroundColor", resources.getColor(R.color.gray))
                    .apply {
                        duration = dur
                    }
            val reverseButtonTextColorToDefaultAnimation =
                ObjectAnimator.ofArgb(x.second, "textColor", resources.getColor(R.color.black))
                    .apply {
                        duration = dur
                    }

            list.add(
                AnimatorSet().apply {
                    play(reverseButtonBackgroundToDefaultAnimation)
                    play(reverseButtonTextColorToDefaultAnimation)
                    play(reverseFontColorToDefaultAnimation)
                }
            )
        }
        AnimatorSet().apply { playSequentially(list)}.start()
    }

    private fun checkRow(doOnEnd: () -> Unit = {}) {
        val result = viewModel.colorLogic()

        val listOfFlipAnimation = mutableListOf<AnimatorSet>()

        result.forEach { triple ->
            listOfFlipAnimation.add(
                triple.first.flipTextView(
                    resources.getColor(triple.third),
                    triple.second
                )
            )
        }

        val a =
            AnimatorSet().apply { playSequentially(listOfFlipAnimation as List<AnimatorSet>) }
        a.doOnEnd {
            doOnEnd()
        }
        a.start()
    }

    private fun TextView.flipTextView(color: Int, button: Button): AnimatorSet {
        val flip90degrees = ObjectAnimator.ofFloat(this, "rotationX", 0f, 90f).apply {
            duration = 100

        }

        val fontColorAnimation = ObjectAnimator.ofArgb(
            this,
            "textColor",
            resources.getColor(R.color.black),
            resources.getColor(R.color.white)
        ).apply {
            duration = 10
        }

        val textViewBackgroundColorAnimation =
            ObjectAnimator.ofArgb(this, "backgroundColor", color).apply {
                duration = 200
            }
        val flip90degreesBack = ObjectAnimator.ofFloat(this, "rotationX", 90f, 0f).apply {
            duration = 100
        }

        val buttonBackgroundColorAnimation =
            ObjectAnimator.ofArgb(button, "backgroundColor", color).apply {
                duration = 10
            }

        return AnimatorSet().apply {
            play(flip90degrees).before(textViewBackgroundColorAnimation)
            play(textViewBackgroundColorAnimation).before(flip90degreesBack)
            play(flip90degreesBack).with(fontColorAnimation)
                .before(buttonBackgroundColorAnimation)
            play(buttonBackgroundColorAnimation)
        }
    }

    var lock = false
    private fun shakeAnimation() {
        if (!lock) {
            lock = true
            val r = viewModel.lettersRow[viewModel.`try`.id]
            ValueAnimator.ofFloat(
                r.x,
                r.x + 20f,
                r.x - 20,
                r.x + 10,
                r.x - 10,
                r.x - 5,
                r.x + 5,
                r.x
            ).apply {
                duration = 400
                addUpdateListener { a ->
                    r.x = a.animatedValue as Float
                }
                doOnEnd { lock = false }
                start()
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
