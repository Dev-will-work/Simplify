package com.example.coursework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import android.icu.text.DateFormat.DAY
import android.net.Uri
import android.os.Build
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResultLauncher
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import com.example.coursework.data.model.LoggedInUser
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlinx.datetime.LocalDateTime
import java.io.InputStream
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.collections.ArrayList
import java.util.Calendar.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

fun leftShift(arr: ArrayList<Double>, diff: Int): ArrayList<Double> {
    var result = arr.takeLast(arr.size - diff) as ArrayList<Double>
    while (result.size < arr.size) {
        result.add(0.0)
    }

    return result
}

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var viewBinding: ActivityMainBinding

    //private var videoCapture: VideoCapture<Recorder>? = null
    //private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService
    private var adapter: LanguageAdapter? = null
    private var mTts: TextToSpeech? = null

    fun clearSavedData() {
        val prefix = filesDir
        if (File("$prefix/statistics.json").exists()) {
            File("$prefix/statistics.json").delete()
        }
        if (File("$prefix/userdata.json").exists()) {
            File("$prefix/userdata.json").delete()
        }
    }

    @ExperimentalTime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        cameraExecutor = Executors.newSingleThreadExecutor()

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val checkIntent = Intent()
        checkIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA

        val prefix = filesDir

        if (!File("$prefix/userdata.json").exists()) {
            val userData = LoggedInUser(UUID.randomUUID().toString(), "guest", "no email", "")
            val jsonData = Json.encodeToString(userData)
            File("$prefix/userdata.json").writeText(jsonData)
        }
        if (!File("$prefix/imagedata.json").exists()) {
            val uri: Uri = Uri.parse("android.resource://com.example.coursework/drawable/avatar")
            ImageStore.image_uri = uri.toString()

            val jsonData = Json.encodeToString(Image(uri.toString()))
            File("$prefix/imagedata.json").writeText(jsonData)
        }

        if (File("$prefix/statistics.json").exists()) {
            val statisticsData: Dummy?
            try {
                statisticsData =
                    Json.decodeFromString<Dummy>(File("$prefix/statistics.json").readText())
                Counters.share_count = statisticsData.share_count
                Counters.simplification_count = statisticsData.simplification_count
                val diff = Counters.current_timestamp - statisticsData.current_timestamp
                if (diff.inWholeDays > 0) {
                    Counters.monthly_count = leftShift(statisticsData.monthly_count, diff.inWholeDays.toInt())
                    Counters.current_timestamp = Counters.current_timestamp
                } else {
                    Counters.monthly_count = statisticsData.monthly_count
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Can't decode statistics data", Toast.LENGTH_SHORT).show()
            }
        }

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
                    adapter = it.data?.getParcelableExtra("adapter")
                    val language = it.data?.getStringExtra("language")
                    if (language != null) {
                        viewBinding.language.text = language
                        val embedding_activation_request = "language:" + language.take(2).lowercase(Locale.getDefault())
                        retrofitRequest(embedding_activation_request, true)
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

        viewBinding.profile.setOnClickListener {
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
            Counters.share_count++
        }

        viewBinding.outputFrame.share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, viewBinding.outputFrame.textField.text.toString())
            startActivity(Intent.createChooser(shareIntent, "Share using"))
            Counters.share_count++
        }

        viewBinding.outputFrame.copy.setOnClickListener {
            val clip = ClipData.newPlainText(null, viewBinding.outputFrame.textField.text.toString())
            clipboard.setPrimaryClip(clip)
        }

        viewBinding.inputFrame.speak.setOnClickListener {
            mTts?.language = Locale.US
            val text = viewBinding.inputFrame.textField.text.toString()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTts?.speak(text,TextToSpeech.QUEUE_ADD,null,null)
            } else {
                mTts?.speak(text, TextToSpeech.QUEUE_ADD, null)
            }
        }

        viewBinding.outputFrame.speak.setOnClickListener {
            mTts?.language = Locale.US
            val text = viewBinding.outputFrame.textField.text.toString()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTts?.speak(text,TextToSpeech.QUEUE_ADD,null,null)
            } else {
                mTts?.speak(text, TextToSpeech.QUEUE_ADD, null)
            }
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
                    retrofitRequest(input)
                }
                else -> {}
            }
            return@setOnEditorActionListener true
        }
    }

    private fun retrofitRequest(request: String, is_service_request: Boolean = false) {
        App.api?.getSimplifiedText(request)?.enqueue(object : Callback<SimplifierData?> {
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

                if (!is_service_request) {
                    response.body()?.output?.let {
                        viewBinding.outputFrame.textField.setText(HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY))
                    }
                    Counters.simplification_count++
                    val last_elem = Counters.monthly_count.last()
                    Counters.monthly_count.set(Counters.monthly_count.size-1, last_elem + 1)
                }
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
                LENGTH_SHORT)
                .show()
        }
    }

    override fun onPause() {
        super.onPause()
        val jsonData = Json.encodeToString(Dummy(Counters.simplification_count, Counters.share_count, Counters.monthly_count, Counters.current_timestamp))
        val prefix = filesDir
        File("$prefix/statistics.json").writeText(jsonData)
    }
}

@Serializable
data class Dummy(val simplification_count: Int, val share_count: Int,
val monthly_count: ArrayList<Double>, val current_timestamp: Instant)

object Counters {
    var simplification_count: Int = 0
    var share_count: Int = 0
    var monthly_count = arrayListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0,0.0, 0.0, 0.0, 0.0, 0.0)
    var current_timestamp: Instant = Clock.System.now()
}