package com.example.wordle.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import com.example.wordle.Letter
import com.example.wordle.R

fun slightlyScaleUpAnimation(textView: TextView): ValueAnimator? {
    return ValueAnimator.ofFloat(textView.scaleX, 1.1f, 1f).apply {
        duration = 100
        addUpdateListener {
            textView.scaleX = animatedValue as Float
            textView.scaleY = animatedValue as Float
        }
        start()
    }
}

fun shakeAnimation(layout: LinearLayout): () -> Unit {
    var lock = false
    return fun() {
        if (!lock) {
            lock = true
            ValueAnimator.ofFloat(
                layout.x,
                layout.x + 20f,
                layout.x - 20f,
                layout.x + 10f,
                layout.x - 10f,
                layout.x - 5f,
                layout.x + 5f,
                layout.x
            ).apply {
                duration = 400
                addUpdateListener { a ->
                    layout.x = a.animatedValue as Float
                }
                doOnEnd { lock = false }
                start()
            }
        }
    }
}

fun flipTextView(
    textView: TextView,
    colorTextView: Int,
    dur: Long = 80L,
    reset: Boolean = false
): AnimatorSet {

    val flip90degrees = ObjectAnimator.ofFloat(textView, "rotationX", 0f, 90f).apply {
        duration = dur
    }

    val fontColorAnimation = ObjectAnimator.ofArgb(
        textView,
        "textColor",
        textView.resources.getColor(R.color.black),
        textView.resources.getColor(R.color.white)
    ).apply {
        duration = 10
    }

    val textViewBackgroundColorAnimation =
        if (reset) ObjectAnimator.ofArgb(textView, "backgroundColor", textView.resources.getColor(R.color.white)).apply {
            duration = dur
            doOnEnd {
                textView.setBackgroundResource(R.drawable.border)
            }
        } else ObjectAnimator.ofArgb(textView, "backgroundColor", colorTextView).apply {
            duration = dur
        }
    val flip90degreesBack = ObjectAnimator.ofFloat(textView, "rotationX", 90f, 0f).apply {
        duration = dur
    }


    return AnimatorSet().apply {
        play(flip90degrees).before(textViewBackgroundColorAnimation)
        play(textViewBackgroundColorAnimation).before(flip90degreesBack)
        play(flip90degreesBack).with(fontColorAnimation)

    }
}

fun flipListOfTextViews(
    textViews: List<TextView>,
    letters: List<Letter>,
    reset: Boolean = false,
    doOnEnd: () -> Unit
): AnimatorSet {
    val animations = textViews.mapIndexed { index, textview ->

        flipTextView(
            textview,
            textview.resources.getColor(letters[index].backgroundColor),
            reset = reset
        )
    }
    return AnimatorSet().apply {
        playSequentially(animations)
        doOnEnd { doOnEnd() }
    }
}