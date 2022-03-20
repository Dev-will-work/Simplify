package com.example.coursework

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coursework.databinding.ActivityHistoryBinding
import java.util.ArrayList

class HistoryActivity : AppCompatActivity(), HistoryAdapter.ItemClickListener {
    private lateinit var viewBinding: ActivityHistoryBinding
    lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_history)

        val sampleHistoryItem1 = HistoryData("13-March-2022  14:15", "Basketball star Brittney Griner is the latest American to be detained in Russia.", "Basketball star Brittney Griner is the latest American to be arrested in Russia.", true)
        val sampleHistoryItem2 = HistoryData("13-March-2022  14:15", "How a luxury watch brand has become the ultimate status symbol for young celebrities.", "How a fashion watch brand has become the ultimate status image for young people.", false)
        val data = arrayListOf(sampleHistoryItem1, sampleHistoryItem2)

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        viewBinding.list.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(data)
        adapter.setClipboard(clipboard)
        adapter.setClickListener(this)
        viewBinding.list.adapter = adapter

        viewBinding.back1.setOnClickListener {
            startActivity(
                Intent(
                    this@HistoryActivity, MainActivity::class.java
                )
            )
            finish()
        }

    }

    override fun onItemClick(view: View?, position: Int) {
//        val elementToAdd = adapter.getItem(position)
//        Toast.makeText(
//            this,
//            "You clicked " + elementToAdd + " on row number " + position,
//            Toast.LENGTH_SHORT
//        ).show()
//
//
//        if (full_data != null) {
//            val temp = full_data?.get(3)
//            var removed = false
//            if (adapter.added_size == 3) {
//                full_data?.removeAt(3)
//                adapter.added_size--
//                removed = true
//            }
//
//            if (!checkAlreadyCached(full_data?.slice(1..4), elementToAdd) &&
//                !checkRestrictedChoices(elementToAdd)) {
//                full_data?.add(1, elementToAdd)
//                adapter.added_size++
//            }
//            if (adapter.added_size == 2 && removed && temp != null) {
//                full_data?.add(3, temp)
//                adapter.added_size++
//            }
//            adapter.dataSet = full_data as ArrayList<String>
//            full_data = null
//            val resultIntent = Intent()
//            resultIntent.putExtra("adapter", adapter)
//            resultIntent.putExtra("language", elementToAdd)
//            setResult(RESULT_OK, resultIntent)
//            finish()
//            return
//        }
//
//        val temp = adapter.dataSet[3]
//        var removed = false
//        if (adapter.added_size == 3) {
//            adapter.dataSet.removeAt(3)
//            adapter.notifyItemRemoved(3)
//            adapter.added_size--
//            removed = true
//        }
//        if (!checkAlreadyCached(this.adapter.dataSet.slice(1..4), elementToAdd) &&
//            !checkRestrictedChoices(elementToAdd)) {
//            adapter.dataSet.add(1, elementToAdd)
//            adapter.notifyDataSetChanged()
//            adapter.added_size++
//        }
//        if (adapter.added_size == 2 && removed) {
//            adapter.dataSet.add(3, temp)
//            adapter.notifyItemInserted(3)
//            adapter.added_size++
//        }
//        val resultIntent = Intent()
//        resultIntent.putExtra("adapter", adapter)
//        resultIntent.putExtra("language", elementToAdd)
//        setResult(RESULT_OK, resultIntent)
//        finish()
    }
}