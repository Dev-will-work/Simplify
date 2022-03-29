package com.example.coursework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.example.coursework.data.model.LoggedInUser
import com.example.coursework.databinding.ActivityProfileBinding
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import com.jjoe64.graphview.series.LineGraphSeries
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.ExperimentalTime


class ProfileActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityProfileBinding

    @ExperimentalTime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile)

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> {
                    val prefix = filesDir
                    val userData: LoggedInUser
                    var imageData: Image? = null
                    if (File("$prefix/userdata.json").exists()) {
                        try {
                            userData =
                                Json.decodeFromString<LoggedInUser>(File("$prefix/userdata.json").readText())
                            viewBinding.nameSurname.text = userData.displayName
                            viewBinding.email.text = userData.email
                        } catch (e: Exception) {
                            Toast.makeText(this, "Can't decode user data", Toast.LENGTH_SHORT).show()
                        }
                    }
                    if (File("$prefix/imagedata.json").exists()) {
                        try {
                            imageData =
                                Json.decodeFromString<Image>(File("$prefix/imagedata.json").readText())
                        } catch (e: Exception) {
                            Toast.makeText(this, "Can't decode image data", Toast.LENGTH_SHORT).show()
                        }
                        viewBinding.avatar.setImageURI(imageData?.image_uri?.toUri())
                        viewBinding.avatar.clipToOutline = true
                        viewBinding.avatar.scaleType = ImageView.ScaleType.CENTER_CROP
                        viewBinding.avatar.background = AppCompatResources.getDrawable(this, R.drawable.rounded)
                    }
                }
                else -> {}
            }
        }

        viewBinding.back1.setOnClickListener {
            startActivity(
                Intent(
                    this@ProfileActivity, MainActivity::class.java
                )
            )
            finish()
        }

        viewBinding.changeInfo.setOnClickListener {
            val i = Intent(this, ChangeProfileActivity::class.java)
            launcher.launch(i)
        }

        val prefix = filesDir
        val userData: LoggedInUser
        var imageData: Image? = null
        if (File("$prefix/userdata.json").exists()) {
            try {
                userData =
                    Json.decodeFromString<LoggedInUser>(File("$prefix/userdata.json").readText())
                viewBinding.nameSurname.text = userData.displayName
                viewBinding.email.text = userData.email
            } catch (e: Exception) {
                Toast.makeText(this, "Can't decode user data", Toast.LENGTH_SHORT).show()
            }
        }
        if (File("$prefix/imagedata.json").exists()) {
            try {
                imageData =
                    Json.decodeFromString<Image>(File("$prefix/imagedata.json").readText())
            } catch (e: Exception) {
                Toast.makeText(this, "Can't decode image data", Toast.LENGTH_SHORT).show()
            }
            viewBinding.avatar.setImageURI(imageData?.image_uri?.toUri())
        }
        if (File("$prefix/statistics.json").exists()) {
            val statisticsData: DummyCounters?
            try {
                statisticsData =
                    Json.decodeFromString<DummyCounters>(File("$prefix/statistics.json").readText())
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

        viewBinding.numberTabLeft.myEditText.setText(Counters.simplification_count.toString())
        viewBinding.numberTabRight.myEditText.setText(Counters.share_count.toString())

        val calendar: Calendar = Calendar.getInstance()

        val dateArray = ArrayList<Date>()
        calendar.add(Calendar.DATE, -20)
        for (i in 0..20) {
            val d: Date = calendar.time
            dateArray.add(d)
            calendar.add(Calendar.DATE, 1)
        }

        try {
            val points = arrayOfNulls<DataPoint>(21)
            for (i in points.indices) {
                points[i] = DataPoint(dateArray[i], Counters.monthly_count[i])
            }
            val series = LineGraphSeries(points)

            series.setOnDataPointTapListener { _, dataPoint ->
                val formatted_X = viewBinding.graph.gridLabelRenderer.labelFormatter.formatLabel(dataPoint.x, true)
                Toast.makeText(
                    this,
                    "$formatted_X: ${dataPoint.y.toInt()}",
                    LENGTH_SHORT
                ).show()
            }

            viewBinding.graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)
            viewBinding.graph.gridLabelRenderer.numHorizontalLabels = 2

            val max = Counters.monthly_count.maxOrNull()

            viewBinding.graph.viewport.isYAxisBoundsManual = true
            viewBinding.graph.viewport.setMinY(0.0)
            val increment: Double
            if (max != null) {
                increment = when (max.toInt() % 4) {
                    0 -> {
                        8.0
                    }
                    1 -> {
                        7.0
                    }
                    2 -> {
                        6.0
                    }
                    3 -> {
                        5.0
                    }
                    else -> {
                        Toast.makeText(this, "Strange max: $max", LENGTH_SHORT).show()
                        8.0
                    }
                }
                viewBinding.graph.viewport.setMaxY(max + increment)
            } else {
                viewBinding.graph.viewport.setMaxY(40.0)
            }

            viewBinding.graph.viewport.setMinX(dateArray.first().time.toDouble())
            viewBinding.graph.viewport.setMaxX(dateArray.last().time.toDouble())
            viewBinding.graph.viewport.isXAxisBoundsManual = true

//            viewBinding.graph.getViewport().setScalable(true);
//            viewBinding.graph.getViewport().setScalableY(true);

            viewBinding.graph.viewport.isScalable = true
            viewBinding.graph.viewport.isScrollable = true // enables horizontal scrolling

            viewBinding.graph.gridLabelRenderer.setHumanRounding(false)

            viewBinding.graph.addSeries(series)

        } catch (e: IllegalArgumentException) {
            Toast.makeText(this@ProfileActivity, e.message, Toast.LENGTH_LONG).show()
        }

    }
}