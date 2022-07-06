package com.example.wordle

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.animation.doOnEnd
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
        Toast.makeText(requireContext(), "word is ${viewModel.wordle}", Toast.LENGTH_LONG).show()

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
                        val toast =
                            Toast.makeText(requireContext(), "Not in word list", Toast.LENGTH_LONG)
                        toast.show()

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
                        checkRow()

                        Toast.makeText(requireContext(), viewModel.wordle, Toast.LENGTH_LONG).show()
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
        while (viewModel.resetStack.isNotEmpty()) {
            val x = viewModel.resetStack.pop()
            x.first.apply {
                text = " "
                setTextColor(resources.getColor(R.color.black))
                setBackgroundResource(R.drawable.border)
            }
            x.second!!.apply {
                setTextColor(resources.getColor(R.color.black))
                setBackgroundColor(resources.getColor(R.color.gray))
            }
        }
    }

    private fun checkRow(doOnEnd: () -> Unit = {}) {
        val result = viewModel.colorLogic()

        val listRowAnimation = mutableListOf<AnimatorSet>()

        result.forEach { pair ->
            listRowAnimation.add(pair.first.flipTextView(resources.getColor(pair.second)))
        }

        val a = AnimatorSet().apply { playSequentially(listRowAnimation as List<AnimatorSet>) }
        a.doOnEnd {
            doOnEnd()
        }
        a.start()
    }

    private fun TextView.flipTextView(color: Int): AnimatorSet {
        val f = ObjectAnimator.ofFloat(this, "rotationX", 0f, 90f).apply {
            duration = 100

        }

        val tc = ObjectAnimator.ofArgb(
            this,
            "textColor",
            resources.getColor(R.color.black),
            resources.getColor(R.color.white)
        ).apply {
            duration = 10
        }

        val colorAnimation =
            ObjectAnimator.ofArgb(this, "backgroundColor", color).apply {
                duration = 200
            }
        val x = ObjectAnimator.ofFloat(this, "rotationX", 90f, 0f).apply {
            duration = 100
        }

        return AnimatorSet().apply {
            play(f).before(colorAnimation)
            play(colorAnimation).before(x)
            play(x).with(tc)
        }
    }
    var lock = false
    private fun shakeAnimation() {
        if (!lock) {
        lock = true
        val r = viewModel.lettersRow[viewModel.`try`.id]
        ValueAnimator.ofFloat(r.x, r.x + 20f, r.x - 20, r.x + 10, r.x - 10, r.x).apply {
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