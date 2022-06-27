package com.example.coursework

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MenuRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityProfileBinding
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.util.*


/**
 * This class handles profile screen and its actions.
 *
 * @property viewBinding
 * Util object, that simplifies access to activity parts.
 */
class ProfileActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityProfileBinding

    private fun askLogout() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.logout_ask_title))

        builder.setPositiveButton(
            getString(R.string.cancel_answer)
        ) { dialog, _ -> //What ever you want to do with the value
            dialog.dismiss()
        }
        builder.setNegativeButton(
            getString(R.string.exit_answer)
        ) { _, _ -> //What ever you want to do with the value
            val prefix = filesDir
            clearFile("$prefix/userdata.json")
            startActivity(
                Intent(this, StartActivity::class.java)
            )
            finish()
        }

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            ?.setTextColor(ContextCompat.getColor(this, R.color.completed_200))

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            ?.setTextColor(ContextCompat.getColor(this, R.color.red))
    }

    /**
     * Function which constructs and shows dropdown menu window.
     *
     * @param v
     * Abstract View, representing menu element.
     * @param menuRes
     * Menu resource ID.
     * @param launcher
     * launcher of another actiivty to move at.
     */
    private fun showMenu(v: View, @MenuRes menuRes: Int, launcher: ActivityResultLauncher<Intent>) {
        val popup = PopupMenu(applicationContext, v)

        popup.menuInflater.inflate(menuRes, popup.menu)

        val lastInd = popup.menu.size() - 1
        val str = SpannableString(popup.menu.getItem(lastInd).title)
        str.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.red)), 0, str.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        popup.menu.getItem(lastInd).title = str

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            // Respond to menu item click.
            when (menuItem.title.toString()) {
                getString(R.string.option_1) -> {
                    val i = Intent(this, ChangeProfileActivity::class.java)
                    launcher.launch(i)
                    true
                }
                getString(R.string.option_2) -> {
                    Toast.makeText(this, "Not implemented yet!", LENGTH_SHORT).show()
                    true
                }
                getString(R.string.option_3) -> {
                    askLogout()
                    true
                }
                else -> { true }
            }
        }
        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popup.show()
    }

    /**
     * Function, executed when the application is opened first time.
     * @receiver
     * This function handles statistics representation and profile screen behaviour.
     *
     * @param savedInstanceState
     * Bundle with simple types, can be used for temporal storage
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        val prefix = filesDir

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> {
                    checkObjectInitialization(CachedUser, this, "$prefix/userdata.json")
                    checkObjectInitialization(ImageStore, this, "$prefix/imagedata.json")

                    viewBinding.nameSurname.text = CachedUser.retrieveUsername()
                    viewBinding.email.text = CachedUser.retrieveEmail()

                }
                else -> {}
            }
        }

        viewBinding.settings.setOnClickListener {
            showMenu(it, R.menu.popup_menu, launcher)
        }



        viewBinding.back1.setOnClickListener {
            startActivity(
                Intent(
                    this@ProfileActivity, MainActivity::class.java
                )
            )
            finish()
        }

        checkObjectInitialization(CachedUser, this, "$prefix/userdata.json")
        checkObjectInitialization(ImageStore, this, "$prefix/imagedata.json")
        checkObjectInitialization(Counters, this, "$prefix/statistics.json")

        viewBinding.nameSurname.text = CachedUser.retrieveUsername()
        viewBinding.email.text = CachedUser.retrieveEmail()

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
                val formattedX = viewBinding.graph.gridLabelRenderer.labelFormatter.formatLabel(dataPoint.x, true)
                Toast.makeText(
                    this,
                    "$formattedX: ${dataPoint.y.toInt()}",
                    LENGTH_SHORT
                ).show()
            }

            viewBinding.graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)
            viewBinding.graph.gridLabelRenderer.numHorizontalLabels = 2
            viewBinding.graph.gridLabelRenderer.horizontalLabelsColor = ContextCompat.getColor(this, R.color.base_400)
            viewBinding.graph.gridLabelRenderer.verticalLabelsColor = ContextCompat.getColor(this, R.color.base_400)


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