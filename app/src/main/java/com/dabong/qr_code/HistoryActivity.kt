package com.dabong.qr_code

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabong.qr_code.databinding.ActivityHistoryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    private var appDatabase: AppDatabase? = null
    private var historyList: List<Barcode>? = null
    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDatabase = AppDatabase.getInstance(this)
        CoroutineScope(IO).launch {
            historyList = appDatabase?.barcodeDao()?.getAll()
            historyList?.let {
                binding.historyListview.adapter = HistoryAdapter(this@HistoryActivity, it)
            }
        }
        Log.e("list", historyList.toString())
        binding.historyListview.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }

    override fun onResume() {
        CoroutineScope(IO).launch {
            historyList = appDatabase?.barcodeDao()?.getAll()
        }

        super.onResume()
    }

}