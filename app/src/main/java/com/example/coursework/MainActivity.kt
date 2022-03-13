package com.example.coursework

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.coursework.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    //private var videoCapture: VideoCapture<Recorder>? = null
    //private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val launcher = registerForActivityResult(StartActivityForResult(), {
            when(it.resultCode) {
                1 -> {
                    val scanned = it.data?.getStringExtra("result")
                    viewBinding.inputFrame.textField.setText(scanned)
                }
                else -> {}
            }
        })

        cameraExecutor = Executors.newSingleThreadExecutor()

        viewBinding.buttons.camera.setOnClickListener {
            val i = Intent(this, PhotoActivity::class.java)
            launcher.launch(i)
        }

        viewBinding.inputFrame.textField.setOnEditorActionListener {
                textView, actionId, keyEvent ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    val input = viewBinding.inputFrame.textField.text.toString()
                    App.api?.getSimplifiedText(input)?.enqueue(object : Callback<SimplifierData?> {
                        override fun onResponse(
                            call: Call<SimplifierData?>,
                            response: Response<SimplifierData?>
                        ) {
                            when (response.code()) {
                                404 -> {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Not found",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return
                                }
                                400 -> {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Bad formed request",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return
                                }
                                401 -> {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Bad API key, not authorized",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return
                                }
                                200 -> {}
                                else -> {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Unknown server answer, try again",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return
                                }
                            }

                            viewBinding.outputFrame.textField.text = Html.fromHtml(response.body()?.output)
                        }

                        override fun onFailure(call: Call<SimplifierData?>, t: Throwable) {
                            Toast.makeText(
                                this@MainActivity,
                                "An error with network occured",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    })
                }
                else -> {}
            }
            return@setOnEditorActionListener true
        }
    }

    private fun captureVideo() {}

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}