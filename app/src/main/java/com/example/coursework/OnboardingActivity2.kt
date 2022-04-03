package com.example.coursework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityOnboarding2Binding

class OnboardingActivity2 : AppCompatActivity() {
    private lateinit var viewBinding: ActivityOnboarding2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding2)
        with (viewBinding) {
            setClickMoveToActivity(skip, this@OnboardingActivity2, StartActivity::class)
            setClickMoveToActivity(next, this@OnboardingActivity2, OnboardingActivity3::class)
        }
    }
}