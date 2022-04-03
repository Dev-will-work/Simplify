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
 * Extension function to simplify setting an afterTextChanged action to EditText components.
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

fun leftShift(arr: ArrayList<Double>, diff: Int): ArrayList<Double> {
    val result = arr.takeLast(arr.size - diff) as ArrayList<Double>
    while (result.size < arr.size) {
        result.add(0.0)
    }
    return result
}


inline fun <reified T> readFile(context: Context, filename: String): T? {
    if (File(filename).exists()) {
        return try {
            Json.decodeFromString<T>(File(filename).readText())
        } catch (e: Exception) {
            Toast.makeText(context, "Can't decode $filename data", Toast.LENGTH_SHORT).show()
            return null
        }
    }
    return null
}

inline fun <reified T> writeFile(filename: String, instance: T) {
    val jsonData = Json.encodeToString(instance)
    File(filename).writeText(jsonData)
}

fun clearFile(filename: String) {
    if (File(filename).exists()) {
        File(filename).delete()
    }
}

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
                val last_elem = Counters.monthly_count.last()
                Counters.monthly_count[Counters.monthly_count.size-1] = last_elem + 1
            }
        }

        override fun onFailure(call: Call<SimplifierData?>, t: Throwable) {
            Toast.makeText(
                context,
                "An error with network occured",
                Toast.LENGTH_LONG
            ).show()
        }

    })
}

fun greeting(ctx: Context) {
    if (!SettingsObject.toggleData.filter {
            it.option_name.contains("greeting")
        }[0].toggle_state) {
        Toast.makeText(ctx, SettingsObject.greeting, Toast.LENGTH_LONG).show()
    }
}

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

interface SharedObject<T> {
    fun defaultInitialization(ctx: Context)
    fun set(ctx: Context, dummy: T)
    fun initialized(): Boolean
}

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