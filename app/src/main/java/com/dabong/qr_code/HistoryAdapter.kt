package com.dabong.qr_code

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class HistoryAdapter(val context: Context, val barcodeList: List<Barcode>) :
    RecyclerView.Adapter<HistoryAdapter.Holder>() {
    private lateinit var builder: AlertDialog.Builder

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val historyIcon: ImageView = itemView.findViewById(R.id.history_Icon)
        private val historyType = itemView.findViewById<TextView>(R.id.history_type)
        private val historyText = itemView.findViewById<TextView>(R.id.history_text)

        fun bind(barcode: Barcode, context: Context) {
            historyType.text = barcode.type
            historyText.text = barcode.result
            itemView.setOnClickListener {
                barcode.type?.let { it1 -> barcode.result?.let { it2 -> readBarcode(it1, it2) } }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return barcodeList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(barcodeList[position], context)
    }

    fun readBarcode(type: String, rawResult: String) {
        builder = AlertDialog.Builder(context)
        when (type) {
            "URI" -> {
                val geoListener = DialogInterface.OnClickListener { _, p1 ->
                    when (p1) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(rawResult)
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }
                builder.setPositiveButton("execute", geoListener)
                builder.setNegativeButton("cancel", geoListener)


            }
            "EMAIL_ADDRESS" -> {
                val emailListener = DialogInterface.OnClickListener { _, p1 ->
                    when (p1) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }
                builder.setPositiveButton("execute", emailListener)
                builder.setNegativeButton("cancel", emailListener)

            }
            "TEL" -> {
                val tel_listener = DialogInterface.OnClickListener { _, p1 ->
                    when (p1) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse(rawResult)
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }
                builder.setPositiveButton("execute", tel_listener)
                builder.setNegativeButton("cancel", tel_listener)

            }
            else -> {
                val listener = DialogInterface.OnClickListener { _, p1 ->
                    when (p1) {
                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }
                builder.setNegativeButton("cancel", listener)
            }
        }
        builder.setTitle(type)
        builder.setMessage(rawResult)
        builder.show()

    }


}