package com.dabong.qr_code

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Barcode::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun barcodeDao(): BarcodeDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "barcodeDB.db"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}