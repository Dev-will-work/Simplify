package com.example.coursework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coursework.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivitySettingsBinding
    lateinit var adapter1: ToggleOptionsAdapter
    lateinit var adapter2: SimpleOptionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        val toggle_data = arrayListOf(
            ToggleOptionsData("Autodetect language", false),
            ToggleOptionsData("Improve simplification", true),
            ToggleOptionsData("Disable input hints", false),
            ToggleOptionsData("Disable onboarding preview", false),
            ToggleOptionsData("Disable notifications", false),
            ToggleOptionsData("Disable greeting", true)
            )
        val data = arrayListOf("About us", "Feedback", "Help", "Rate our app")

        viewBinding.toggleList.layoutManager = LinearLayoutManager(this)
        viewBinding.simpleList.layoutManager = LinearLayoutManager(this)
        adapter1 = ToggleOptionsAdapter(toggle_data)
        adapter2 = SimpleOptionsAdapter(data)
        viewBinding.toggleList.adapter = adapter1
        viewBinding.simpleList.adapter = adapter2

        viewBinding.back.setOnClickListener {
            startActivity(
                Intent(
                    this@SettingsActivity, MainActivity::class.java
                )
            )
            finish()
        }
    }
}