package com.example.coursework

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityOnboarding1Binding
import kotlin.time.ExperimentalTime


/**
 * This class handles first onboarding screen and its actions.
 *
 * @property viewBinding
 * Util object, that simplifies access to activity parts.
 */
class OnboardingActivity1 : AppCompatActivity() {
    private lateinit var viewBinding: ActivityOnboarding1Binding

    /**
     * Function, executed when the application is opened first time.
     * @receiver
     * This function handles greeting and first onboarding screen behaviour.
     *
     * @param savedInstanceState
     * Bundle with simple types, can be used for temporal storage
     */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding1)
        val prefix = filesDir

        checkObjectInitialization(SettingsObject, this, "$prefix/settingsdata.json")

        greeting(this)

        val disableOnboarding = SettingsObject.isPropertyToggled("onboarding")
        if (disableOnboarding) {
            startActivity(
                Intent(
                    this@OnboardingActivity1, StartActivity::class.java
                )
            )
            finish()
            return
        }

        with(viewBinding) {
            setClickMoveToActivity(skip, this@OnboardingActivity1, StartActivity::class)
            setClickMoveToActivity(next, this@OnboardingActivity1, OnboardingActivity2::class)
        }
    }
}