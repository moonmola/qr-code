package com.dabong.qr_code

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Barcode(
    @PrimaryKey(autoGenerate = true) val uid: Long,
    @ColumnInfo(name = "type") val type: String?,
    @ColumnInfo(name = "result") val result: String?
)
