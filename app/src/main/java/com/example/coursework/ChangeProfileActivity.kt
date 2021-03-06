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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityChangeProfileBinding
import kotlinx.serialization.Serializable

/**
 * This class handles changes in user profile.
 *
 * @property viewBinding
 * Util object, that simplifies access to activity parts.
 * @property launcherImage
 * Activity launcher for image processing.
 * @property resultUri
 * Temporary variable storage uri
 */
class ChangeProfileActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityChangeProfileBinding
    private lateinit var launcherImage: ActivityResultLauncher<Intent>
    private lateinit var resultUri: Uri

    /**
     * Function that calls image pick from a gallery.
     *
     */
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        launcherImage.launch(intent)
    }

    /**
     * Function which checks permissions and starts image selection from the gallery
     * if all permissions are given.
     */
    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                val permissionCoarse = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                requestPermissions(permission, 1001)
                requestPermissions(permissionCoarse, 1002)
            } else {
                pickImageFromGallery()
            }
        }
    }

    /**
     * Function, executed when the application is opened first time.
     * @receiver
     * This handles change of user data from input fields.
     *
     * @param savedInstanceState
     * Bundle with simple types, can be used for temporal storage
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_change_profile)

        val prefix = filesDir

        checkObjectInitialization(ImageStore, this, "$prefix/imagedata.json")

//        viewBinding.avatar.setImageURI(ImageStore.image_uri.toUri())
//        viewBinding.avatar.clipToOutline = true
//        viewBinding.avatar.scaleType = ImageView.ScaleType.CENTER_CROP
//        viewBinding.avatar.background = AppCompatResources.getDrawable(this, R.drawable.rounded)

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

        launcherImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> {
//                    viewBinding.avatar.setImageURI(it.data?.data)
//                    it.data?.data?.let { uri ->
//                        resultUri = uri
//                    }
//                    viewBinding.avatar.clipToOutline = true
//                    viewBinding.avatar.scaleType = ImageView.ScaleType.CENTER_CROP
//                    viewBinding.avatar.background = AppCompatResources.getDrawable(this, R.drawable.rounded)
                }
                else -> {}
            }
        }

        viewBinding.back.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

//        viewBinding.avatar.setOnClickListener {
//            checkPermissionForImage()
//        }

        viewBinding.save.setOnClickListener {
            checkObjectInitialization(CachedUser, this, "$prefix/userdata.json")

            if (viewBinding.username.textField1.text.toString() != "" &&
                viewBinding.email.textField1.text.toString() != "" &&
                viewBinding.password.textField1.text.toString() != "") {
                val newUsername = viewBinding.username.textField1.text.toString()
                val newEmail = viewBinding.email.textField1.text.toString()
                val newPassword = viewBinding.password.textField1.text.toString()
                // ID managed internally later
                CachedUser.set(this, LoggedInUser("", newUsername, newEmail, newPassword))
                setResult(RESULT_OK)
            } else {
                setResult(RESULT_CANCELED)
            }

//            val imageData = Image(resultUri.toString())
//            writeFile("$prefix/imagedata.json", imageData)
            finish()
        }
    }
}

/**
 * Util class for serialization of user image data.
 *
 * @property image_uri
 * Image uri that will be serialized.
 */
@Serializable
data class Image(var image_uri: String)

/**
 * Object that provides uri of user image to another components.
 * Compliant to SharedObject interface.
 * @property image_uri
 * Uri of the user image.
 */
object ImageStore : SharedObject<Image> {
    lateinit var image_uri: String

    /**
     * Function which tells if this object is ready to use.
     * @receiver
     * returns true if image uri is initialized.
     *
     * @return Bool, that determines if this object properties are fully initialized and ready.
     *
     */
    override fun initialized(): Boolean {
        return ::image_uri.isInitialized
    }

    /**
     * Function for setting default values if the last state is missing.
     * @receiver
     * sets image uri to default avatar image.
     *
     * @param ctx
     * Context of the application.
     */
    override fun defaultInitialization(ctx: Context) {
        image_uri = Uri.parse("android.resource://com.example.coursework/drawable/avatar").toString()
    }

    /**
     * This function sets deserialized data to object for further use.
     * @receiver
     * Sets image uri from dummy to provider object.
     *
     * @param ctx
     * Context of the application.
     * @param dummy
     * Class of the similar structure, needed for serialization.
     */
    override fun set(ctx: Context, dummy: Image) {
        this.image_uri = dummy.image_uri
    }
}