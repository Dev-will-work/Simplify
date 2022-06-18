package com.example.coursework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityOnboarding2Binding

/**
 * This class handles second onboarding screen and its actions.
 *
 * @property viewBinding
 * Util object, that simplifies access to activity parts.
 */
class OnboardingActivity2 : AppCompatActivity() {
    private lateinit var viewBinding: ActivityOnboarding2Binding

    /**
     * Function, executed when the application is opened first time.
     * @receiver
     * This function handles second onboarding screen behaviour.
     *
     * @param savedInstanceState
     * Bundle with simple types, can be used for temporal storage
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding2)
        with (viewBinding) {
            setClickMoveToActivity(skip, this@OnboardingActivity2, StartActivity::class)
            setClickMoveToActivity(next, this@OnboardingActivity2, OnboardingActivity3::class)
        }
    }
}