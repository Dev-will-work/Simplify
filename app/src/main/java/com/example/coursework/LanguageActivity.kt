package com.example.coursework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityLanguageBinding

import android.content.Intent
import android.view.inputmethod.EditorInfo


class LanguageActivity : AppCompatActivity(), LanguageAdapter.ItemClickListener {
    lateinit var adapter: LanguageAdapter
    private lateinit var viewBinding: ActivityLanguageBinding
    var full_data: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_language)

        val data = arrayListOf("Recently used", "All languages", "Arabic", "Bulgarian", "Catalan", "Czech", "Danish", "Dutch",
            "English", "Finnish", "French", "German", "Hungarian", "Indonesian", "Italian",
            "Norwegian", "Polish", "Portuguese", "Romanian", "Russian", "Spanish", "Swedish",
            "Turkish", "Ukrainian")

        viewBinding.list.layoutManager = LinearLayoutManager(this)

        val cached_adapter = intent.getParcelableExtra<LanguageAdapter>("adapter")
        if (cached_adapter != null) {
            adapter = cached_adapter
        } else {
            adapter = LanguageAdapter(data)
        }
        adapter.setClickListener(this)
        val currentLanguage = intent.getStringExtra("current_language")!!
        adapter.setCurrentLanguage(currentLanguage)
        viewBinding.list.adapter = adapter

        viewBinding.back.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("adapter", adapter)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        full_data = adapter.dataSet

        viewBinding.searchFrame.editTextTextPersonName.setOnEditorActionListener {
                textView, actionID, keyEvent ->
            when (actionID) {
                EditorInfo.IME_ACTION_DONE -> {
                    adapter.dataSet = ArrayList(full_data!!.filter {
                        it.matches(
                            viewBinding.searchFrame.editTextTextPersonName.text.toString()
                                .toRegex()
                        )
                    })
                    adapter.notifyDataSetChanged()
                }
            }
            return@setOnEditorActionListener true
        }
    }

    fun checkAlreadyCached(dataset: List<String>, s: String): Boolean {
        for (cache in dataset) {
            if (cache == "All languages") {
                return false
            } else if (s == cache) {
                return true
            }
        }
        return false
    }

    fun checkRestrictedChoices(s: String): Boolean {
        return listOf("All languages", "Recently used").contains(s)
    }

    override fun onItemClick(view: View?, position: Int) {
        val elementToAdd = adapter.getItem(position)
        Toast.makeText(
            this,
            "You clicked " + elementToAdd + " on row number " + position,
            Toast.LENGTH_SHORT
        ).show()


        if (full_data != null) {
            val temp = full_data!![3]
            var removed = false
            if (adapter.added_size == 3) {
                full_data!!.removeAt(3)
//                adapter.notifyItemRemoved(3)
                adapter.added_size--
                removed = true
            }

            if (!checkAlreadyCached(full_data!!.slice(1..4), elementToAdd) &&
                !checkRestrictedChoices(elementToAdd)) {
                full_data!!.add(1, elementToAdd)
//                adapter.notifyDataSetChanged()
                adapter.added_size++
            }
            if (adapter.added_size == 2 && removed) {
                full_data!!.add(3, temp)
//                adapter.notifyItemInserted(3)
                adapter.added_size++
            }
            adapter.dataSet = full_data as ArrayList<String>
            full_data = null
            val resultIntent = Intent()
            resultIntent.putExtra("adapter", adapter)
            resultIntent.putExtra("language", elementToAdd)
            setResult(RESULT_OK, resultIntent)
            finish()
            return
        }

        val temp = adapter.dataSet[3]
        var removed = false
        if (adapter.added_size == 3) {
            adapter.dataSet.removeAt(3)
            adapter.notifyItemRemoved(3)
            adapter.added_size--
            removed = true
        }
        if (!checkAlreadyCached(this.adapter.dataSet.slice(1..4), elementToAdd) &&
            !checkRestrictedChoices(elementToAdd)) {
            adapter.dataSet.add(1, elementToAdd)
            adapter.notifyDataSetChanged()
            adapter.added_size++
        }
        if (adapter.added_size == 2 && removed) {
            adapter.dataSet.add(3, temp)
            adapter.notifyItemInserted(3)
            adapter.added_size++
        }
        val resultIntent = Intent()
        resultIntent.putExtra("adapter", adapter)
        resultIntent.putExtra("language", elementToAdd)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}