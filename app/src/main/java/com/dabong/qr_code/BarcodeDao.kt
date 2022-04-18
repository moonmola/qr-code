package com.dabong.qr_code

import androidx.room.*

@Dao
interface BarcodeDao {
    @Query("SELECT * FROM barcode")
    fun getAll(): List<Barcode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg barcode: Barcode)

    @Delete
    fun delete(barcode: Barcode)
}