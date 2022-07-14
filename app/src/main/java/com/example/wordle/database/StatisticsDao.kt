package com.example.wordle.database

import android.content.Context
import androidx.room.*

@Dao
interface StatisticsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(stat: StatisticEntity)

    @Query("SELECT * FROM StatisticEntity WHERE uid IS 1")
    fun getStat(): StatisticEntity?

    @Update
    fun update(newStat: StatisticEntity)
}

@Database(entities = [StatisticEntity::class], version = 1)
abstract class StatisticDatabase: RoomDatabase() {
    abstract fun statisticDao(): StatisticsDao

    companion object {
        @Volatile
        private var INSTANCE: StatisticDatabase? = null

        fun getDatabase(context: Context): StatisticDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StatisticDatabase::class.java,
                    "statistic_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}