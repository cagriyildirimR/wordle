package com.example.wordle

import android.app.Application
import com.example.wordle.database.StatisticDatabase

class WordleApplication : Application(){

    val database by lazy { StatisticDatabase.getDatabase(this) }
}