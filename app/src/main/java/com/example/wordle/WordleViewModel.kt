package com.example.wordle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordle.data.WORD_LENGTH
import com.example.wordle.data.wordList
import com.example.wordle.data.wordListSize
import com.example.wordle.databinding.FragmentGameScreenBinding
import com.example.wordle.util.listToWord
import com.example.wordle.util.wordlistBinarySearch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*

const val EMPTY_STRING = ""
const val NUMBER_OF_ROWS = 6
val DEFAULT_LETTER = Letter(" ", R.drawable.border, R.color.black)
const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
const val ALPHABET_LENGTH = 26
val DEFAULT_KEY = Key(backgroundColor = R.color.gray, textColor = R.color.black)

class WordleViewModel() : ViewModel() {

    val signal = MutableSharedFlow<Signal>()

    var r = Random(System.nanoTime()).nextInt(wordListSize)
    private val wordSlice get() = r * WORD_LENGTH

    val wordle get() = wordList.slice(wordSlice until wordSlice + WORD_LENGTH)

    val listOfTextViews =
        List(NUMBER_OF_ROWS) { List(WORD_LENGTH) { MutableStateFlow(Letter(" ")) } }

    val listOfKeys = mutableMapOf<String, MutableStateFlow<Key>>().apply {
        ALPHABET.forEach { letter ->
            this[letter.toString()] = MutableStateFlow<Key>(
                DEFAULT_KEY
            )
        }
    }

    val currentPosition = Position(0, 0)

    val letterTrash = Stack<MutableStateFlow<Letter>>()
    val keyTrash = Stack<MutableStateFlow<Key>>()

    fun setLetter(letter: String) {
        if (currentPosition.col < WORD_LENGTH) {
            viewModelScope.launch {
                listOfTextViews[currentPosition.row][currentPosition.col].emit(Letter(letter))
            }
            currentPosition.nextColumn()
        }
    }

    fun deleteLetter() {
        if (currentPosition.col > 0) {
            currentPosition.previousColumn()
            viewModelScope.launch {
                listOfTextViews[currentPosition.row][currentPosition.col].emit(Letter(" "))
            }
        }
    }

    fun resetGame() {
        currentPosition.reset()
        viewModelScope.launch {
            while (!letterTrash.empty()) {
                letterTrash.pop().emit(DEFAULT_LETTER)
                keyTrash.pop().emit(DEFAULT_KEY)
            }
        }
        getNewWordle()
    }

    var guess: String = ""

    suspend fun checkRow() {
        guess =
            listToWord(listOfTextViews[currentPosition.row].filter { it.value.letter != " " }
                .map { it.value.letter }).lowercase(
                Locale.getDefault()
            )
        when {
            guess.length < 5 -> {
                signal.emit(Signal.NEEDLETTER)
            }
            wordle == guess -> {
                signal.emit(Signal.WIN)
            }
            wordlistBinarySearch(wordList, guess, 0, wordListSize, WORD_LENGTH) -> {
                if (currentPosition.row == 5) {
                    signal.emit(Signal.GAMEOVER)
                } else {
                    signal.emit((Signal.NEXTTRY))
                }
            }
            else -> {
                signal.emit(Signal.NOTAWORD)
            }
        }
    }

    fun checkColor(): List<Letter> {
        val list = mutableListOf<Letter>()
        listOfTextViews[currentPosition.row].forEachIndexed { index, flow ->
            list.add(when (guess[index]) {
                wordle[index] -> {
                    Letter(flow.value.letter, R.color.green, R.color.white)
                }
                in wordle.filterIndexed { i, s -> guess[i] != s } -> {
                    Letter(flow.value.letter, R.color.yellow, R.color.white)
                }
                else -> {
                    Letter(flow.value.letter, R.color.dark_gray, R.color.white)
                }
            })
        }
        return list
    }

    fun emitColor(list: List<Letter> = checkColor()) {
        viewModelScope.launch {
            listOfTextViews[currentPosition.row].forEachIndexed { index, flow ->
                val letter = list[index]
                flow.emit(letter)
                letterTrash.push(flow)

                val key = listOfKeys[letter.letter]!!
                val bg = when (key.value.backgroundColor) {
                    R.color.green -> R.color.green
                    R.color.yellow -> if (letter.backgroundColor == R.color.green) R.color.green else R.color.yellow
                    else -> letter.backgroundColor
                }

                key.emit(Key(bg, letter.textColor))
                keyTrash.push(key)
            }
        }
    }

    private fun getNewWordle() {
        r = Random(System.nanoTime()).nextInt(wordListSize)
    }
}

data class Position(var row: Int, var col: Int) {
    fun nextColumn() {
        col += 1
    }

    fun previousColumn() {
        col -= 1
    }

    fun nextRow() {
        row += 1
        col = 0
    }

    fun reset() {
        row = 0
        col = 0
    }
}

data class Letter(
    val letter: String,
    val backgroundColor: Int = R.drawable.border,
    val textColor: Int = R.color.black
)

data class Key(val backgroundColor: Int, val textColor: Int)