package com.example.coursework

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coursework.databinding.ActivityHistoryBinding
import kotlin.collections.ArrayList

class HistoryActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityHistoryBinding
    lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_history)

        val sampleHistoryItem1 = HistoryData("13-March-2022  14:15", "Basketball star Brittney Griner is the latest American to be detained in Russia.", "Basketball star Brittney Griner is the latest American to be arrested in Russia.", true)
        val sampleHistoryItem2 = HistoryData("13-March-2022  14:15", "How a luxury watch brand has become the ultimate status symbol for young celebrities.", "How a fashion watch brand has become the ultimate status image for young people.", false)

        val data = try {
            HistoryAdapterObject.dataset
        } catch (e: Exception) {
            Toast.makeText(this, "Static history adapter N/A", Toast.LENGTH_SHORT).show()
            arrayListOf<HistoryData>()
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        viewBinding.list.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(data)
        adapter.setClipboard(clipboard)
        viewBinding.list.adapter = adapter
        val full_dataSet = data

        viewBinding.back1.setOnClickListener {
            startActivity(
                Intent(
                    this@HistoryActivity, MainActivity::class.java
                )
            )
            finish()
        }

        viewBinding.searchFrame.editTextTextPersonName.addTextChangedListener {
            changed ->
            if (changed != null) {
                if (changed.toString() == "") {
                    adapter.dataSet = full_dataSet
                } else {
                    adapter.dataSet = full_dataSet.filter {
                        (it.date?.contains(changed.toString()) == true) or
                                (it.request?.contains(changed.toString()) == true) or
                                (it.response?.contains(changed.toString()) == true)
                    } as ArrayList<HistoryData>
                }
                adapter.notifyDataSetChanged()
            }
        }

    }

    override fun onPause() {
        super.onPause()
        val prefix = filesDir

        val historydata = DummyHistoryAdapter(HistoryAdapterObject.dataset)
        writeFile("$prefix/historydata.json", historydata)
    }
}