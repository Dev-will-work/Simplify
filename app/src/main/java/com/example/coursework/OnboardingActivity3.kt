package com.example.coursework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityOnboarding3Binding

class OnboardingActivity3 : AppCompatActivity() {
    private lateinit var viewBinding: ActivityOnboarding3Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding3)
        with (viewBinding) {
            setClickMoveToActivity(skip, this@OnboardingActivity3, StartActivity::class)
            setClickMoveToActivity(getStarted, this@OnboardingActivity3, StartActivity::class)
        }
    }
}