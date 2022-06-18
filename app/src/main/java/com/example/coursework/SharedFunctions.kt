package com.example.coursework

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import kotlin.reflect.KClass

//binding = ActivityXBinding.inflate(layoutInflater)
//setContentView(binding.root)
// or
//private lateinit var viewBinding: ActivityXBinding
//viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_x)

/**
 * Function, which make it simpler to observe text changes in input field.
 *
 * @param afterTextChanged
 * Function, which needs to be executed with the text when it is changed.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

/**
 * Util function, that moves all array elements to the left on [diff] positions,
 * removing first [diff] elements.
 *
 * @param arr
 * Array to change.
 * @param diff
 * Number, representing shift to left of each element.
 * @return ArrayList with shifted elements.
 */
fun leftShift(arr: ArrayList<Double>, diff: Int): ArrayList<Double> {
    val result = arr.takeLast(arr.size - diff) as ArrayList<Double>
    while (result.size < arr.size) {
        result.add(0.0)
    }
    return result
}

/**
 * Util function, that moves all array elements to the right on [diff] positions,
 * removing last [diff] elements.
 *
 * @param arr
 * Array to change.
 * @param diff
 * Number, representing shift to right of each element.
 * @return ArrayList with shifted elements.
 */
fun rightShift(arr: ArrayList<Double>, diff: Int): ArrayList<Double> {
    val result = arr.take(arr.size - diff) as ArrayList<Double>
    while (result.size < arr.size) {
        result.add(0, 0.0)
    }
    return result
}

/**
 * Function, which reads [T] class data from serialized file.
 *
 * @param T
 * Class with data, which we try to decode.
 * @param context
 * Application context.
 * @param filename
 * @return [T] instance if the file is decoded successfully, else null.
 */
inline fun <reified T> readFile(context: Context, filename: String): T? {
    if (File(filename).exists()) {
        return try {
            Json.decodeFromString<T>(File(filename).readText())
        } catch (e: Exception) {
            Toast.makeText(context, "Can't decode $filename data", Toast.LENGTH_SHORT).show()
            null
        }
    }
    return null
}

/**
 * Function, which reads [T] class data from serialized file.
 *
 * @param T
 * Type of the class, which data we will write to file.
 * @param filename
 * Name of the file with serialized data.
 * @param instance
 * Instance of the class, which needs to be serialized.
 */
inline fun <reified T> writeFile(filename: String, instance: T) {
    val jsonData = Json.encodeToString(instance)
    File(filename).writeText(jsonData)
}

/**
 * Util function, which deletes wrong serialized file.
 *
 * @param filename
 * Name of the file to delete.
 */
fun clearFile(filename: String) {
    if (File(filename).exists()) {
        File(filename).delete()
    }
}

/**
 * Function, which
 *
 * @param context
 * @param request
 * @param user_id
 * @param out_field
 * @param is_service_request
 */
fun retrofitRequest(context: Context, request: String, user_id: String, out_field: MyEditText? = null, is_service_request: Boolean = false) {
    App.api?.getSimplifiedText(request, user_id)?.enqueue(object : Callback<SimplifierData?> {
        override fun onResponse(
            call: Call<SimplifierData?>,
            response: Response<SimplifierData?>
        ) {
            when (response.code()) {
                404 -> {
                    Toast.makeText(
                        context,
                        "Not found",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
                400 -> {
                    Toast.makeText(
                        context,
                        "Bad formed request",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
                401 -> {
                    Toast.makeText(
                        context,
                        "Bad API key, not authorized",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
                200 -> {}
                else -> {
                    Toast.makeText(
                        context,
                        "Unknown server answer, try again",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
            }

            if (!is_service_request) {
                response.body()?.output?.let {
                    val historyItem = HistoryData(Clock.System.now().toString(), request, HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(), false)
                    HistoryAdapterObject.dataset.add(0, historyItem)
                    if (HistoryAdapterObject.dataset.size == 101) {
                        HistoryAdapterObject.dataset.removeLast()
                    }
                    out_field?.setText(HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY))
                }
                Counters.simplification_count++
                val lastElem = Counters.monthly_count.last()
                Counters.monthly_count[Counters.monthly_count.size-1] = lastElem + 1
            }
        }

        override fun onFailure(call: Call<SimplifierData?>, t: Throwable) {
            Toast.makeText(
                context,
                "An error with network occurred",
                Toast.LENGTH_LONG
            ).show()
        }

    })
}

/**
 * Function that shows user greeting if it is on in settings.
 *
 * @param ctx
 * application context.
 */
fun greeting(ctx: Context) {
    if (!SettingsObject.toggleData.filter {
            it.option_name.contains("greeting")
        }[0].toggle_state) {
        Toast.makeText(ctx, SettingsObject.greeting, Toast.LENGTH_LONG).show()
    }
}

/**
 * Function, that simplifies move to another [dest] activity from the current.
 *
 * @param view
 * View, on which we set click listener with move to another activity.
 * @param ctx
 * application Context.
 * @param dest
 * Another activity we want to move at.
 */
fun setClickMoveToActivity(view: View, ctx: Context, dest: KClass<*>) {
    view.setOnClickListener {
        ContextCompat.startActivity(
            ctx,
            Intent(
                ctx, dest.java
            ), Bundle()
        )
        (ctx as Activity).finish()
    }
}

/**
 * Interface, which templates the behaviour of  objects, that share data between components.
 *
 * @param T
 * Type of the class, that is used by the object as a dummy for serialization.
 */
interface SharedObject<T> {
    /**
     * Function for setting default values if the last state is missing.
     *
     * @param ctx
     * Context of the application.
     */
    fun defaultInitialization(ctx: Context)

    /**
     * This function sets deserialized data to object for further use.
     *
     * @param ctx
     * Context of the application.
     * @param dummy
     * Class of the similar structure, needed for serialization.
     */
    fun set(ctx: Context, dummy: T)

    /**
     * Function which tells if this object is ready to use.
     *
     * @return Bool, that determines if this object properties are fully initialized and ready.
     *
     */
    fun initialized(): Boolean
}

/**
 * Helper function, which takes the data sharing object and checks if it is ready to work.
 *
 * @param T
 * Type of the class that we will try to decode from serialized file with [filename],
 * which is very similar by structure to [obj] data.
 * @param obj
 * Sharing data object, that we check on readiness
 * @param ctx
 * Application context.
 * @param filename
 * Name of the file with object serialized data that we will try to decode.
 */
inline fun <reified T> checkObjectInitialization(obj: SharedObject<T>, ctx: Context, filename: String) {
    if (!obj.initialized()) {
        val data = readFile<T>(ctx, filename)
        if (data != null) {
            obj.set(ctx, data)
        } else {
            Toast.makeText(ctx, "Bad $filename file!", Toast.LENGTH_LONG).show()
            obj.defaultInitialization(ctx)
        }
    }
}