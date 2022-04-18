package com.dabong.qr_code

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result
import com.google.zxing.client.result.EmailAddressParsedResult
import com.google.zxing.client.result.EmailAddressResultParser
import com.google.zxing.client.result.ParsedResultType
import com.google.zxing.client.result.ResultParser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.dm7.barcodescanner.zxing.ZXingScannerView

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    var mContext: Context? = null

    private var mScannerView: ZXingScannerView? = null
    private lateinit var builder: androidx.appcompat.app.AlertDialog.Builder
    private var appDatabase: AppDatabase? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mScannerView = findViewById(R.id.barcodeScanner)

        requestPermission.launch(android.Manifest.permission.CAMERA)

        premission_button.setOnClickListener {
            requestPermission.launch(android.Manifest.permission.CAMERA)
        }

        mContext = this

        builder = AlertDialog.Builder(this)
        appDatabase = AppDatabase.getInstance(this)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.history -> {
                val nextIntent = Intent(this, HistoryActivity::class.java)
                startActivity(nextIntent)
            }
            else -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        mScannerView?.let {
            it.setAutoFocus(true)
            it.setResultHandler(this)
            it.startCamera()
        }
    }


    fun readBarcode(rawResult: Result) {
        val result = ResultParser.parseResult(rawResult)
        when (result.type) {
            ParsedResultType.GEO -> {
                val geoListener = DialogInterface.OnClickListener { _, p1 ->
                    when (p1) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(rawResult.text)
                            }
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            }
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            onResume()
                        }
                    }
                }
                builder.setPositiveButton("execute", geoListener)
                builder.setNegativeButton("cancel", geoListener)


            }
            ParsedResultType.URI -> {
                val url_listener = DialogInterface.OnClickListener { _, p1 ->
                    when (p1) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawResult.toString()))
                            startActivity(intent)
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            onResume()
                        }
                    }
                }
                builder.setPositiveButton("execute", url_listener)
                builder.setNegativeButton("cancel", url_listener)
            }
            ParsedResultType.EMAIL_ADDRESS -> {
                val emailListener = DialogInterface.OnClickListener { _, p1 ->
                    when (p1) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val parsedResult = result as EmailAddressParsedResult
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, parsedResult.tos)
                                putExtra(Intent.EXTRA_SUBJECT, parsedResult.subject)
                                putExtra(Intent.EXTRA_TEXT, parsedResult.body)

                            }
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            }
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            onResume()
                        }
                    }
                }
                builder.setPositiveButton("execute", emailListener)
                builder.setNegativeButton("cancel", emailListener)

            }
            ParsedResultType.TEL -> {
                val telListener = DialogInterface.OnClickListener { _, p1 ->
                    when (p1) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse(rawResult.text)
                            }
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            }
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            onResume()
                        }
                    }
                }
                builder.setPositiveButton("execute", telListener)
                builder.setNegativeButton("cancel", telListener)

            }
            else -> {
                val listener = DialogInterface.OnClickListener { _, p1 ->
                    when (p1) {
                        DialogInterface.BUTTON_NEGATIVE -> {
                            onResume()
                        }
                    }
                }
                builder.setNegativeButton("cancel", listener)

            }
        }
        builder.setTitle(result.type.toString())
        builder.setMessage(rawResult.text)
        builder.show()

    }

    override fun handleResult(rawResult: Result) {
        Toast.makeText(applicationContext, "?", Toast.LENGTH_LONG).show()
        val result = ResultParser.parseResult(rawResult)

        CoroutineScope(IO).launch {
            appDatabase?.barcodeDao()
                ?.insertAll(Barcode(0, result.type.toString(), rawResult.text))
        }
        Log.e("DDdisplay", EmailAddressResultParser.parseResult(rawResult).toString())
        readBarcode(rawResult)

    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                premission_button.visibility = View.GONE
                Log.d("DEBUG", "permission granted")

            } else {
                Log.d("DEBUG", "permission denied")
                val permissionListener = DialogInterface.OnClickListener { _, p1 ->
                    when (p1) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }
                builder.setTitle("Notice")
                builder.setMessage("Camera permission is required for this function. Please set permission")
                builder.setPositiveButton("setting", permissionListener)
                builder.setNegativeButton("cancel", permissionListener)
                builder.show()
            }
        }

}
