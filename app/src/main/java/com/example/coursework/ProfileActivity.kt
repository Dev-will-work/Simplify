package com.example.coursework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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


class ProfileActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile)

        viewBinding.back1.setOnClickListener {
            startActivity(
                Intent(
                    this@ProfileActivity, MainActivity::class.java
                )
            )
            finish()
        }

        val prefix = getFilesDir()
        var userData: LoggedInUser? = null
        if (File("$prefix/userdata.json").exists()) {
            try {
                userData =
                    Json.decodeFromString<LoggedInUser>(File("$prefix/userdata.json").readText())
            } catch (e: Exception) {
                Toast.makeText(this, "Can't decode user data", Toast.LENGTH_SHORT).show()
            }
        }

        viewBinding.nameSurname.text = userData?.displayName
        viewBinding.email.text = userData?.email

        viewBinding.numberTabLeft.myEditText.setText(Counters.simplification_count.toString())
        viewBinding.numberTabRight.myEditText.setText(Counters.share_count.toString())

        val calendar: Calendar = Calendar.getInstance()

        val dateArray = ArrayList<Date>()
        calendar.add(Calendar.DATE, -10)
        for (i in 10 downTo 0) {
            val d: Date = calendar.getTime()
            dateArray.add(d)
            calendar.add(Calendar.DATE, 1)
        }
        val d: Date = calendar.getTime()
        dateArray.add(d)
        calendar.add(Calendar.DATE, 1)
        for (i in 0..10) {
            val d: Date = calendar.getTime()
            dateArray.add(d)
            calendar.add(Calendar.DATE, 1)
        }

        try {
            val points = arrayOfNulls<DataPoint>(21)
            for (i in points.indices) {
                points[i] = DataPoint(dateArray[i], Math.sin(i * 0.5) * 20 * (Math.random() * 10 + 1))
            }
            val series = LineGraphSeries(points)

            viewBinding.graph.getGridLabelRenderer().setLabelFormatter(DateAsXAxisLabelFormatter(this));
            viewBinding.graph.getGridLabelRenderer().setNumHorizontalLabels(2)

            viewBinding.graph.getViewport().setYAxisBoundsManual(true);
            viewBinding.graph.getViewport().setMinY(-150.0);
            viewBinding.graph.getViewport().setMaxY(150.0);

            viewBinding.graph.getViewport().setMinX(dateArray.first().getTime().toDouble());
            viewBinding.graph.getViewport().setMaxX(dateArray.last().getTime().toDouble());
            viewBinding.graph.getViewport().isXAxisBoundsManual = true;

//            viewBinding.graph.getViewport().setScalable(true);
//            viewBinding.graph.getViewport().setScalableY(true);

            viewBinding.graph.getViewport().setScalable(true);
            viewBinding.graph.getViewport().setScrollable(true); // enables horizontal scrolling

            viewBinding.graph.getGridLabelRenderer().setHumanRounding(false);

            viewBinding.graph.addSeries(series)

        } catch (e: IllegalArgumentException) {
            Toast.makeText(this@ProfileActivity, e.message, Toast.LENGTH_LONG).show()
        }

    }
}