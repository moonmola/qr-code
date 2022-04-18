package com.dabong.qr_code

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dabong.qr_code.databinding.ListItemBinding


class HistoryAdapter(private val context: Context, private val barcodeList: List<Barcode>) :
    RecyclerView.Adapter<HistoryAdapter.VH>() {
    private lateinit var builder: AlertDialog.Builder

    inner class VH(private val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(barcode: Barcode) {
            binding.historyIcon.setImageDrawable(when (barcode.type) {
                "URI" -> ContextCompat.getDrawable(context, R.drawable.ic_baseline_link_24)
                "TEL" -> ContextCompat.getDrawable(context, R.drawable.ic_baseline_call_24)
                "EMAIL_ADDRESS" -> ContextCompat.getDrawable(context, R.drawable.ic_baseline_email_24)
                else-> ContextCompat.getDrawable(context, R.drawable.ic_baseline_text_snippet_24)
            })
            binding.historyType.text = barcode.type
            binding.historyText.text = barcode.result
            binding.root.setOnClickListener {
                barcode.type?.let { it1 -> barcode.result?.let { it2 -> readBarcode(it1, it2) } }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int {
        return barcodeList.size
    }

    override fun onBindViewHolder(VH: VH, position: Int) {
        VH.bind(barcodeList[position])
    }

    fun readBarcode(type: String, rawResult: String) {
        builder = AlertDialog.Builder(context)
        when (type) {
            "URI" -> {
                val geoListener = DialogInterface.OnClickListener { _, p1 ->
                    when (p1) {
                        DialogInterface.BUTTON_NEGATIVE -> {
                            copyClipBoard(type,rawResult)
                        }
                        DialogInterface.BUTTON_POSITIVE -> {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(rawResult)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
                builder.setPositiveButton("execute", geoListener)
                builder.setNegativeButton("copy", geoListener)
                builder.setNeutralButton("cancel", geoListener)
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
                    }
                }
                builder.setPositiveButton("execute", emailListener)
                builder.setNeutralButton("cancel", emailListener)

            }
            "TEL" -> {
                val telListener = DialogInterface.OnClickListener { _, p1 ->
                    when (p1) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse(rawResult)
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        }
                    }
                }
                builder.setPositiveButton("execute", telListener)
                builder.setNeutralButton("cancel", telListener)

            }
            else -> {
                val listener = DialogInterface.OnClickListener { _, p1 ->
                    when (p1) {
                        DialogInterface.BUTTON_NEGATIVE -> {
                            copyClipBoard(type,rawResult)
                        }
                    }
                }
                builder.setNegativeButton("copy", listener)
                builder.setNeutralButton("cancel", listener)
            }
        }
        builder.setTitle(type)
        builder.setMessage(rawResult)
        builder.show()

    }
    private fun copyClipBoard(type: String, rawResult: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(type,rawResult)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
    }


}