package com.example.wordle

enum class State(val id: Int) {
    TRY1(0), TRY2(1), TRY3(2), TRY4(3), TRY5(4), TRY6(5)
}

enum class Signal {
    NOTAWORD,
    NEEDLETTER,
    NEXTTRY,
    GAMEOVER,
    WIN
}