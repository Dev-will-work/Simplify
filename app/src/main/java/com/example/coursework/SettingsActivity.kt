package com.example.coursework

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.FILL_PARENT
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coursework.SettingsActivity
import com.example.coursework.data.model.LoggedInUser
import com.example.coursework.databinding.ActivitySettingsBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import java.io.FileNotFoundException
import java.io.InputStream
import android.text.Editable

import android.content.DialogInterface
import com.example.coursework.data.model.CachedUser


class MyPreferences(context: Context?) {

    companion object {
        private const val DARK_STATUS = "io.github.manuelernesto.DARK_STATUS"
    }

    private val preferences = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }

    var darkMode = preferences?.getInt(DARK_STATUS, 0)
        set(value) = value?.let { preferences?.edit()?.putInt(DARK_STATUS, it)?.apply() }!!

}

class SettingsActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivitySettingsBinding
    lateinit var adapter1: ToggleOptionsAdapter
    lateinit var adapter2: SimpleOptionsAdapter

    private fun changeGreeting() {
        val edittext = MyEditText(this);
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change greeting")

        builder.setView(edittext)

        builder.setPositiveButton("Save"
        ) { _, _ -> //What ever you want to do with the value
            val youEditTextValue: String = edittext.text.toString()
            SettingsObject.greeting = youEditTextValue
            viewBinding.greetingFrame.email.text = youEditTextValue
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun chooseThemeDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.choose_theme_text))
        val styles = arrayOf("Light", "Dark", "System default")
        val checkedItem = MyPreferences(this).darkMode

        if (checkedItem != null) {
            builder.setSingleChoiceItems(styles, checkedItem) { dialog, which ->

                when (which) {
                    0 -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        MyPreferences(this).darkMode = 0
                        delegate.applyDayNight()
                        viewBinding.themeFrame.secondText = "Day"
                        dialog.dismiss()
                    }
                    1 -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        MyPreferences(this).darkMode = 1
                        delegate.applyDayNight()
                        viewBinding.themeFrame.secondText = "Night"
                        dialog.dismiss()
                    }
                    2 -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        MyPreferences(this).darkMode = 2
                        delegate.applyDayNight()
                        viewBinding.themeFrame.secondText = "As in the system"
                        dialog.dismiss()
                    }

                }
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun checkTheme() {
        when (MyPreferences(this).darkMode) {
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                delegate.applyDayNight()
                viewBinding.themeFrame.secondText = "Day"
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                delegate.applyDayNight()
                viewBinding.themeFrame.secondText  = "Night"
            }
            2 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                delegate.applyDayNight()
                viewBinding.themeFrame.secondText = "As in the system"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        val prefix = filesDir
        viewBinding.toggleList.layoutManager = LinearLayoutManager(this)
        viewBinding.simpleList.layoutManager = LinearLayoutManager(this)
        adapter1 = ToggleOptionsAdapter()
        adapter2 = SimpleOptionsAdapter(SettingsObject.simpleData)
        viewBinding.toggleList.adapter = adapter1
        viewBinding.simpleList.adapter = adapter2

        checkObjectInitialization(CachedUser, this, "$prefix/userdata.json")
        checkObjectInitialization(ImageStore, this, "$prefix/imagedata.json")
        checkObjectInitialization(SettingsObject, this, "$prefix/settingsdata.json")

        viewBinding.avatarFrame.firstText = CachedUser.retrieveUsername()
        viewBinding.avatarFrame.secondText = CachedUser.retrieveEmail()

        viewBinding.avatarFrame.avatar1 = Uri.parse(ImageStore.image_uri).toString()
        viewBinding.avatarFrame.avatar.clipToOutline = true
        viewBinding.avatarFrame.avatar.scaleType = ImageView.ScaleType.CENTER_CROP
        viewBinding.avatarFrame.avatar.background = AppCompatResources.getDrawable(this, R.drawable.rounded)

        viewBinding.avatarFrame.rightIcon.setOnClickListener {
            CachedUser.defaultInitialization(this)
        }

        viewBinding.back.setOnClickListener {
            startActivity(
                Intent(
                    this@SettingsActivity, MainActivity::class.java
                )
            )
            finish()
        }

        checkTheme()
        viewBinding.themeFrame.rightIcon.setOnClickListener {
            chooseThemeDialog()
        }
        viewBinding.themeFrame.avatar.setOnClickListener {
            chooseThemeDialog()
        }
        viewBinding.themeFrame.email.setOnClickListener {
            chooseThemeDialog()
        }
        viewBinding.themeFrame.nameSurname.setOnClickListener {
            chooseThemeDialog()
        }

        viewBinding.greetingFrame.rightIcon.setOnClickListener {
            changeGreeting()
        }
        viewBinding.greetingFrame.avatar.setOnClickListener {
            changeGreeting()
        }
        viewBinding.greetingFrame.email.setOnClickListener {
            changeGreeting()
        }
        viewBinding.greetingFrame.nameSurname .setOnClickListener {
            changeGreeting()
        }
    }

    override fun onPause() {
        super.onPause()
        val prefix = filesDir
        with(SettingsObject) {
            simpleData = adapter2.dataSet
            writeFile("$prefix/settingsdata.json", DummySettingsAdapters(toggleData, simpleData, greeting))
        }
    }
}

//if (File("$prefix/imagedata.json").exists()) {
//    var yourDrawable: Drawable
//    imageData = readFile<Image>(this, "$prefix/imagedata.json")
//        ?: Image("android.resource://com.example.coursework/drawable/avatar")
//    try {
//        val inputStream: InputStream? = contentResolver.openInputStream(imageData.image_uri.toUri())
//        yourDrawable = Drawable.createFromStream(inputStream, imageData.image_uri.toUri().toString())
//    } catch (e: FileNotFoundException) {
//        yourDrawable = ResourcesCompat.getDrawable(resources, R.drawable.avatar, null)!!
//    }
//}
//
//viewBinding.avatarFrame.avatar1 = yourDrawable