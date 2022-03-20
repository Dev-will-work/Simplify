package com.example.coursework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityProfileBinding

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
    }
}