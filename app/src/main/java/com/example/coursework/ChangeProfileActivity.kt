package com.example.coursework

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.example.coursework.data.model.LoggedInUser
import com.example.coursework.databinding.ActivityChangeProfileBinding
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

class ChangeProfileActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityChangeProfileBinding
    lateinit var launcher_image: ActivityResultLauncher<Intent>
    lateinit var result_uri: Uri

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        launcher_image.launch(intent) // GIVE AN INTEGER VALUE FOR IMAGE_PICK_CODE LIKE 1000
    }

    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                val permissionCoarse = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                requestPermissions(permission, 1001) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
                requestPermissions(permissionCoarse, 1002) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
            } else {
                pickImageFromGallery()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_change_profile)

        val prefix = filesDir
        var imageData: Image? = null
        if (File("$prefix/imagedata.json").exists()) {
            try {
                imageData =
                    Json.decodeFromString<Image>(File("$prefix/imagedata.json").readText())
            } catch (e: Exception) {
                Toast.makeText(this, "Can't decode image data", Toast.LENGTH_SHORT).show()
            }
            viewBinding.avatar.setImageURI(imageData?.image_uri?.toUri())
            viewBinding.avatar.clipToOutline = true
            viewBinding.avatar.scaleType = ImageView.ScaleType.CENTER_CROP
            viewBinding.avatar.background = AppCompatResources.getDrawable(this, R.drawable.rounded)
        }

        var show = true
        viewBinding.password.eye.setOnClickListener {
            if (show) {
                viewBinding.password.textField1.inputType = (InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PASSWORD or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                show = false
            } else {
                viewBinding.password.textField1.inputType = (InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                show = true
            }
            viewBinding.password.textField1.maxLines = 4
            viewBinding.password.textField1.clearFocus()
        }

        launcher_image = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> {
                    viewBinding.avatar.setImageURI(it.data?.data)
                    it.data?.data?.let {
                        result_uri = it
                    }
                    viewBinding.avatar.clipToOutline = true
                    viewBinding.avatar.scaleType = ImageView.ScaleType.CENTER_CROP
                    viewBinding.avatar.background = AppCompatResources.getDrawable(this, R.drawable.rounded)
                }
                else -> {}
            }
        }

        viewBinding.back.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        viewBinding.avatar.setOnClickListener {
            checkPermissionForImage()
        }

        viewBinding.save.setOnClickListener {
            val userData: LoggedInUser
            if (File("$prefix/userdata.json").exists()) {
                userData = try {
                    Json.decodeFromString<LoggedInUser>(File("$prefix/userdata.json").readText())
                } catch (e: Exception) {
                    Toast.makeText(this, "Can't decode user data", Toast.LENGTH_SHORT).show()
                    LoggedInUser(UUID.randomUUID().toString(), "guest", "no email", "")
                }
            } else {
                userData = LoggedInUser(UUID.randomUUID().toString(), "guest", "no email", "")
            }

            if (viewBinding.username.textField1.text.toString() != "") {
                userData.displayName = viewBinding.username.textField1.text.toString()
            }
            if (viewBinding.email.textField1.text.toString() != "") {
                userData.email = viewBinding.email.textField1.text.toString()
            }
            if (viewBinding.password.textField1.text.toString() != "") {
                userData.password = viewBinding.password.textField1.text.toString()
            }

            val jsonData = Json.encodeToString(userData)
            File("$prefix/userdata.json").writeText(jsonData)

            val jsonImageData = Json.encodeToString(Image(result_uri.toString()))
            File("$prefix/imagedata.json").writeText(jsonImageData)

            setResult(RESULT_OK)
            finish()
        }
    }
}

@Serializable
data class Image(var image_uri: String)

object ImageStore : SharedObject<Image> {
    lateinit var image_uri: String

    override fun initialized(): Boolean {
        return ::image_uri.isInitialized
    }

    override fun defaultInitialization(ctx: Context) {
        image_uri = Uri.parse("android.resource://com.example.coursework/drawable/avatar").toString()
    }

    override fun set(ctx: Context, dummy: Image) {
        this.image_uri = dummy.image_uri
    }
}