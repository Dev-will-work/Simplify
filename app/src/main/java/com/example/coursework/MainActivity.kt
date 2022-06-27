package com.example.coursework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.example.coursework.databinding.ActivityMainBinding
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.Engine.CHECK_VOICE_DATA_PASS
import android.widget.Toast.LENGTH_SHORT
import android.content.*
import android.content.Intent
import android.os.Build
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResultLauncher
import androidx.databinding.DataBindingUtil
import com.example.coursework.Counters.current_timestamp
import com.example.coursework.Counters.monthly_count
import com.example.coursework.Counters.old_timestamp
import com.example.coursework.Counters.share_count
import com.example.coursework.Counters.simplification_count
import com.example.coursework.ImageStore.image_uri
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Class, that handles main screen and its interactive parts.
 * @property viewBinding
 * Util object, that simplifies access to activity parts.
 * @property adapter
 * Adapter class for storing and rendering languages.
 * @property cameraExecutor
 * Object, responsive for making photos and access to the camera.
 * @property mTts
 * Object, responsive for Text to Speech service.
 */
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private var adapter: LanguageAdapter? = null
    private var mTts: TextToSpeech? = null
    //private var videoCapture: VideoCapture<Recorder>? = null
    //private var recording: Recording? = null

    /**
     * Function, executed when the application is opened first time.
     * @receiver
     * Setups camera, text fields and other interactive behaviour on the main screen.
     *
     * @param savedInstanceState
     * Bundle with simple types, can be used for temporal storage
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        cameraExecutor = Executors.newSingleThreadExecutor()

        val prefix = filesDir

        checkObjectInitialization(HistoryAdapterObject, this, "$prefix/historydata.json")
        checkObjectInitialization(CachedUser, this, "$prefix/userdata.json")
        checkObjectInitialization(ImageStore, this, "$prefix/imagedata.json")
        checkObjectInitialization(Counters, this, "$prefix/statistics.json")

        val checkIntent = Intent()
        checkIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
        val launcherTts = registerForActivityResult(StartActivityForResult()) {
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
        launcherTts.launch(checkIntent)

        setClickMoveToActivity(viewBinding.settings, this, SettingsActivity::class)
        setClickMoveToActivity(viewBinding.buttons.bookmarks, this, HistoryActivity::class)
        setClickMoveToActivity(viewBinding.profile, this, ProfileActivity::class)

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
            share_count++
        }

        viewBinding.outputFrame.share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, viewBinding.outputFrame.textField.text.toString())
            startActivity(Intent.createChooser(shareIntent, "Share using"))
            share_count++
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

        val launcherVoice = registerForActivityResult(StartActivityForResult()) {
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
        viewBinding.buttons.microphone.setOnClickListener {
            getSpeechInput(launcherVoice)
        }

        val launcherLanguage = registerForActivityResult(StartActivityForResult()) {
            val languageCodes = HashMap<String, String>()
            val languages = applicationContext.resources.getStringArray(R.array.languages)
                .filter { predicate -> !predicate.contains(' ') }
            val codes = arrayListOf("ar", "bg", "ca", "cs", "da", "nl", "en", "fi", "fr", "de",
                "hu", "id", "it", "no", "pl", "pt", "ro", "ru", "es", "sv", "tr", "uk")
            for ((language, code) in languages.zip(codes))
                languageCodes[language] = code

            when (it.resultCode) {
                RESULT_OK -> {
                    adapter = it.data?.getParcelableExtra("adapter")
                    val language = it.data?.getStringExtra("language")
                    if (language != null) {
                        viewBinding.language.secondText = language
                        val embeddingActivationRequest = "language:" + languageCodes[language]
                        retrofitRequest(this, embeddingActivationRequest, CachedUser.retrieveID() , null, true)
                    }
                }
                else -> {}
            }
        }
        viewBinding.language.root.setOnClickListener {
            val i = Intent(this, LanguageActivity::class.java)
            if (adapter != null) {
                i.putExtra("adapter", adapter)
            }
            val currentLanguage = viewBinding.language.secondText
            i.putExtra("current_language", currentLanguage)
            launcherLanguage.launch(i)
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
        viewBinding.buttons.camera.setOnClickListener {
            val i = Intent(this, PhotoActivity::class.java)
            launcher.launch(i)
        }

        viewBinding.inputFrame.textField.setOnEditorActionListener {
                _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    val input = viewBinding.inputFrame.textField.text.toString()
                    retrofitRequest(this, input, CachedUser.retrieveID(), viewBinding.outputFrame.textField)
                }
                else -> {}
            }
            return@setOnEditorActionListener true
        }
    }

    /**
     * Function for capturing video, not used now and plays role of a mock for future.
     *
     */
    private fun captureVideo() {}

    /**
     * Lifecycle function, which executes when the app is going to die.
     * @receiver
     * Executes safe shutdown for camera and Text To Speech service.
     *
     */
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        mTts?.shutdown()
    }

    /**
     * Function which executes some actions when Text to Speech service is ready.
     *
     * @param p0
     * Integer, representing status of Text to Speech service?
     */
    override fun onInit(p0: Int) {
//        Toast.makeText(applicationContext, "Initialized!", LENGTH_SHORT).show()
    }

    /**
     * Function which starts speech recognition.
     *
     * @param launcher
     * launcher to execute special intent.
     */
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

    /**
     * Lifecycle function, which executes when the app is out of focus.
     * @receiver
     * Serializes statistics and previous requests data.
     *
     */
    override fun onPause() {
        super.onPause()
        val prefix = filesDir

        val statisticsData = DummyCounters(simplification_count, share_count, monthly_count, current_timestamp, old_timestamp)
        writeFile("$prefix/statistics.json", statisticsData)

        val historydata = DummyHistoryAdapter(HistoryAdapterObject.dataset)
        writeFile("$prefix/historydata.json", historydata)
    }
}

/**
 * Util class for serialization of statistics data.
 *
 * @property simplification_count
 * Integer, representing how many times user requested the simplified text.
 * @property share_count
 * Integer, representing how many times user shared the simplified text.
 * @property monthly_count
 * Array with integers, representing user activity for last 20 days
 * @property current_timestamp
 * timestamp that is equal to now at this moment.
 * @property old_timestamp
 * timestamp of the last time user used the application.
 */
@Serializable
data class DummyCounters(val simplification_count: Int, val share_count: Int,
                         val monthly_count: ArrayList<Double>, val current_timestamp: Instant,
                         val old_timestamp: Instant,)

/**
 * Object that provides statistics and timestamps to another components.
 * Compliant to SharedObject interface.
 * @property simplification_count
 * Integer, representing how many times user requested the simplified text.
 * @property share_count
 * Integer, representing how many times user shared the simplified text.
 * @property monthly_count
 * Array with integers, representing user activity for last 20 days
 * @property current_timestamp
 * timestamp that is equal to now at this moment.
 * @property old_timestamp
 * timestamp of the last time user used the application.
 */
object Counters : SharedObject<DummyCounters> {
    var simplification_count: Int = 0
    var share_count: Int = 0
    lateinit var monthly_count: ArrayList<Double>
    lateinit var current_timestamp: Instant
    lateinit var old_timestamp: Instant

    /**
     * Function which tells if this object is ready to use.
     * @receiver
     * returns true if monthly count and timestamps are initialized.
     *
     * @return Bool, that determines if this object properties are fully initialized and ready.
     *
     */
    override fun initialized(): Boolean {
        return ::monthly_count.isInitialized && ::current_timestamp.isInitialized && ::old_timestamp.isInitialized
    }

    /**
     * Function for setting default values if the last state is missing.
     * @receiver
     * sets statistics counts to 0 and timestamps to now.
     *
     * @param ctx
     * Context of the application.
     */
    override fun defaultInitialization(ctx: Context) {
        simplification_count = 0
        share_count = 0
        monthly_count = arrayListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0,0.0, 0.0, 0.0, 0.0, 0.0)
        current_timestamp = Clock.System.now()
        old_timestamp = Clock.System.now()
    }

    /**
     * This function sets deserialized data to object for further use.
     * @receiver
     * Sets statistics and timestamps from dummy to provider object.
     *
     * @param ctx
     * Context of the application.
     * @param dummy
     * Class of the similar structure, needed for serialization.
     */
    override fun set(ctx: Context, dummy: DummyCounters) {
        dummy.monthly_count.sum()
        simplification_count = dummy.simplification_count
        share_count = dummy.share_count
        old_timestamp = dummy.current_timestamp
        current_timestamp = Clock.System.now()
        val diff = current_timestamp - old_timestamp
        monthly_count = when {
            diff.inWholeDays > 0 -> {
                leftShift(dummy.monthly_count, diff.inWholeDays.toInt())
            }
            diff.inWholeDays < 0 -> {
                rightShift(dummy.monthly_count, kotlin.math.abs(diff.inWholeDays.toInt()))
            }
            else -> {
                dummy.monthly_count
            }
        }
    }
}