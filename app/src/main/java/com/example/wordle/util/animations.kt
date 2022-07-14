package com.example.wordle.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import com.example.wordle.Letter
import com.example.wordle.R

fun slightlyScaleUpAnimation(textView: TextView, dur: Long = 100): ValueAnimator {
    return ValueAnimator.ofFloat(textView.scaleX, 1.1f, 1f).apply {
        duration = dur
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
        doOnEnd {
            textView.setTextColor(textView.resources.getColor(R.color.white))
        }
    }
//
//    val fontColorAnimation = ObjectAnimator.ofArgb(
//        textView,
//        "textColor",
//        textView.resources.getColor(R.color.black),
//
//    ).apply {
//        duration = 10
//    }

    val textViewBackgroundColorAnimation =
        if (reset) ObjectAnimator.ofArgb(
            textView,
            "backgroundColor",
            textView.resources.getColor(R.color.white)
        ).apply {
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
        interpolator = AccelerateDecelerateInterpolator()

        play(flip90degrees).before(textViewBackgroundColorAnimation)
        play(textViewBackgroundColorAnimation).before(flip90degreesBack)
        play(flip90degreesBack)

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

fun waveAnimation(tw: TextView, force: Float = 1f, dur: Long): ObjectAnimator {
    return ObjectAnimator.ofFloat(
        tw,
        "translationY",
        tw.translationY + 40,
        tw.translationY - 40,
        tw.translationY + 20,
        tw.translationY - 20,
        tw.translationY + 10,
        tw.translationY - 10,
        tw.translationY
        ).apply {
        duration = dur
    }
}

fun winAnimator(textViews: List<TextView>, doOnEnd: () -> Unit): AnimatorSet {
    val dur = 200L
    return AnimatorSet().apply {
        playSequentially(
            textViews.mapIndexed { inx, tw ->
                //tw.bringToFront()
                slightlyScaleUpAnimation(tw, dur)
            }
        )
        doOnEnd {
            doOnEnd()
        }
    }
}

fun flipMessage(tw: TextView, message: String, doOnEnd: (ts: Float) -> Unit): AnimatorSet {
    val textSize = tw.textSize
    val flip90degrees = ObjectAnimator.ofFloat(tw, "rotationX", 0f, 90f).apply {
        duration = 100
        doOnEnd {
            tw.setTextColor(tw.resources.getColor(R.color.white))
            tw.setBackgroundColor(tw.resources.getColor(R.color.black))
            tw.textSize = 24f
            tw.text = message
        }
    }
    val flip90degreesBack = ObjectAnimator.ofFloat(tw, "rotationX", 90f, 0f).apply {
        duration = 100
    }
    return AnimatorSet().apply {
        playSequentially(flip90degrees, flip90degreesBack)
        doOnEnd {
            doOnEnd(textSize)
        }
    }
}

fun flipToWordle(tw: TextView, message: String, textSize:Float, doOnEnd: () -> Unit): AnimatorSet {
    val flip90degrees = ObjectAnimator.ofFloat(tw, "rotationX", 0f, 90f).apply {
        duration = 100
        doOnEnd {
            tw.setTextColor(tw.resources.getColor(R.color.black))
            tw.setBackgroundColor(tw.resources.getColor(R.color.white))
            tw.textSize = textSize
            tw.text = message
        }
    }
    val flip90degreesBack = ObjectAnimator.ofFloat(tw, "rotationX", 90f, 0f).apply {
        duration = 100
    }
    return AnimatorSet().apply {
        playSequentially(flip90degrees, flip90degreesBack)
        doOnEnd {
            doOnEnd()
        }
    }
}

fun showInfo(tw:TextView, message: String) {
    tw.text = message
    val alpha = ObjectAnimator.ofFloat(tw, "alpha", 0f, 1f, 1f, 1f, 1f, 1f, 0f).apply {
        duration = 2000L
    }.start()
}