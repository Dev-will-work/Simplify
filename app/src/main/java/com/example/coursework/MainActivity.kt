package com.example.coursework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.example.coursework.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.Engine.CHECK_VOICE_DATA_PASS
import android.widget.Toast.LENGTH_SHORT

import android.content.*
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResultLauncher
import androidx.databinding.DataBindingUtil


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var viewBinding: ActivityMainBinding

    //private var videoCapture: VideoCapture<Recorder>? = null
    //private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService
    private var adapter: HistoryAdapter? = null
    private var mTts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        cameraExecutor = Executors.newSingleThreadExecutor()

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val checkIntent = Intent()
        checkIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA

        val launcher = registerForActivityResult(StartActivityForResult()) {
            when (it.resultCode) {
                1 -> {
                    val scanned = it.data?.getStringExtra("result")
                    viewBinding.inputFrame.textField.setText(scanned)
                }
                else -> {}
            }
        }

        val launcher_language = registerForActivityResult(StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> {
                    adapter = it.data?.getParcelableExtra<HistoryAdapter>("adapter")
                    val language = it.data?.getStringExtra("language")
                    if (language != null) {
                        viewBinding.language.text = language
                    }
                }
                else -> {}
            }
        }

        val launcher_voice = registerForActivityResult(StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK ->
                {
                    if (it.data != null) {
                        val result =
                            it.data?.
                            getStringArrayListExtra(
                                RecognizerIntent.EXTRA_RESULTS)
                        if (result != null) {
                            viewBinding.inputFrame.textField.setText(result[0])
                        }
                    }
                }
            }
        }

        val launcher_tts = registerForActivityResult(StartActivityForResult()) {
            when (it.resultCode) {
                CHECK_VOICE_DATA_PASS -> {
                    mTts = TextToSpeech(this, this)
                }
                else -> {
                    // missing data, install it
                    val installIntent = Intent()
                    installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                    startActivity(installIntent)
                }
            }
        }
        launcher_tts.launch(checkIntent)

        viewBinding.settings.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity, SettingsActivity::class.java
                )
            )
            finish()
        }

        viewBinding.buttons.bookmarks.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity, HistoryActivity::class.java
                )
            )
            finish()
        }

        viewBinding.profile?.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity, ProfileActivity::class.java
                )
            )
            finish()
        }

        viewBinding.inputFrame.copy.setOnClickListener {
            val clip = ClipData.newPlainText(null, viewBinding.inputFrame.textField.text.toString())
            clipboard.setPrimaryClip(clip)
        }

        viewBinding.inputFrame.remove.setOnClickListener {
            viewBinding.inputFrame.textField.setText(" ")
        }

        viewBinding.inputFrame.share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, viewBinding.inputFrame.textField.text.toString())
            startActivity(Intent.createChooser(shareIntent, "Share using"))
        }

        viewBinding.outputFrame.share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, viewBinding.outputFrame.textField.text.toString())
            startActivity(Intent.createChooser(shareIntent, "Share using"))
        }

        viewBinding.outputFrame.copy.setOnClickListener {
            val clip = ClipData.newPlainText(null, viewBinding.outputFrame.textField.text.toString())
            clipboard.setPrimaryClip(clip)
        }

        viewBinding.inputFrame.speak.setOnClickListener {
            mTts?.language = Locale.US
            mTts?.speak(viewBinding.inputFrame.textField.text.toString(), TextToSpeech.QUEUE_ADD, null)
        }

        viewBinding.outputFrame.speak.setOnClickListener {
            mTts?.language = Locale.US
            mTts?.speak(viewBinding.outputFrame.textField.text.toString(), TextToSpeech.QUEUE_ADD, null)
        }

        viewBinding.buttons.microphone.setOnClickListener {
            getSpeechInput(launcher_voice)
        }

        viewBinding.language.setOnClickListener {
            val i = Intent(this, LanguageActivity::class.java)
            if (adapter != null) {
                i.putExtra("adapter", adapter)
            }
            val currentLanguage = viewBinding.language.text
            i.putExtra("current_language", currentLanguage)
            launcher_language.launch(i)
        }

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

                            viewBinding.outputFrame.textField.setText(Html.fromHtml(response.body()?.output))
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
        mTts?.shutdown()
    }

    override fun onInit(p0: Int) {
        Toast.makeText(applicationContext, "Hello!", LENGTH_SHORT).show()
    }

    private fun getSpeechInput(launcher: ActivityResultLauncher<Intent>)
    {
        val intent = Intent(RecognizerIntent
            .ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault())

        if (intent.resolveActivity(packageManager) != null)
        {
            launcher.launch(intent)
        } else
        {
            Toast.makeText(this,
                "Your Device Doesn't Support Speech Input",
                Toast.LENGTH_SHORT)
                .show()
        }
    }
}