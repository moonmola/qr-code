package com.dabong.qr_code

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    private var appDatabase: AppDatabase? = null
    private var historyList: List<Barcode>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        val historyView: RecyclerView = findViewById(R.id.history_listview)


        appDatabase = AppDatabase.getInstance(this)
        CoroutineScope(IO).launch {
            historyList = appDatabase?.barcodeDao()?.getAll()
            historyList?.let {
                val mAdapter = HistoryAdapter(this@HistoryActivity, it)
                historyView.adapter = mAdapter
            }
        }
        Log.e("list", historyList.toString())
        historyView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }

    override fun onResume() {
        CoroutineScope(IO).launch {
            historyList = appDatabase?.barcodeDao()?.getAll()
        }

        super.onResume()
    }

}