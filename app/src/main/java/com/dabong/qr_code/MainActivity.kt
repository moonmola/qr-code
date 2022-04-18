package com.dabong.qr_code

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
import androidx.core.content.ContextCompat
import com.dabong.qr_code.databinding.ActivityMainBinding
import com.google.zxing.Result
import com.google.zxing.client.result.EmailAddressParsedResult
import com.google.zxing.client.result.EmailAddressResultParser
import com.google.zxing.client.result.ParsedResultType
import com.google.zxing.client.result.ResultParser
import kotlinx.android.synthetic.main.activity_main.premission_button
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.dm7.barcodescanner.zxing.ZXingScannerView

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private lateinit var builder: AlertDialog.Builder
    private var appDatabase: AppDatabase? = null
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermission.launch(android.Manifest.permission.CAMERA)

        premission_button.setOnClickListener {
            requestPermission.launch(android.Manifest.permission.CAMERA)
        }

        builder = AlertDialog.Builder(this)
        appDatabase = AppDatabase.getInstance(this)
        binding.barcodeScanner.also {
            it.setAutoFocus(true)
            it.setBorderColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
            it.setBorderStrokeWidth(20)
            it.setSquareViewFinder(true)
            it.setIsBorderCornerRounded(true)
            it.setResultHandler(this)
        }
        binding.historyButton.setOnClickListener {
            val nextIntent = Intent(this, HistoryActivity::class.java)
            startActivity(nextIntent)
        }

    }

    override fun onResume() {
        super.onResume()
        binding.barcodeScanner.startCamera()
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeScanner.stopCamera()
    }


    private fun readBarcode(rawResult: Result) {
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
