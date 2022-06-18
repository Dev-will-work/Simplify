package com.example.coursework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityOnboarding3Binding

/**
 * This class handles third onboarding screen and its actions.
 *
 * @property viewBinding
 * Util object, that simplifies access to activity parts.
 */
class OnboardingActivity3 : AppCompatActivity() {
    private lateinit var viewBinding: ActivityOnboarding3Binding

    /**
     * Function, executed when the application is opened first time.
     * @receiver
     * This function handles third onboarding screen behaviour.
     *
     * @param savedInstanceState
     * Bundle with simple types, can be used for temporal storage
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding3)
        with (viewBinding) {
            setClickMoveToActivity(skip, this@OnboardingActivity3, StartActivity::class)
            setClickMoveToActivity(getStarted, this@OnboardingActivity3, StartActivity::class)
        }
    }
}