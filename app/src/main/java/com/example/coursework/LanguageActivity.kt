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
import androidx.core.widget.addTextChangedListener
import java.io.File
import java.util.ArrayList


class LanguageActivity : AppCompatActivity(), LanguageAdapter.ItemClickListener {
    lateinit var adapter: LanguageAdapter
    private lateinit var viewBinding: ActivityLanguageBinding
    lateinit var full_data: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_language)
        val prefix = filesDir

        checkObjectInitialization(LanguageObject, this, "$prefix/languagedata.json")

        viewBinding.list.layoutManager = LinearLayoutManager(this)

        adapter = LanguageAdapter(LanguageObject.dataset, LanguageObject.base_size, LanguageObject.added_size)

        adapter.setClickListener(this)
        val currentLanguage = intent.getStringExtra("current_language")
        if (currentLanguage != null) {
            adapter.setCurrentLanguage(currentLanguage)
        }
        viewBinding.list.adapter = adapter

        viewBinding.back.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        full_data = adapter.dataSet

        viewBinding.searchFrame.editTextTextPersonName.addTextChangedListener {
                changed ->
            if (changed != null) {
                if (changed.toString() == "") {
                    adapter.dataSet = full_data
                } else {
                    adapter.dataSet = full_data.filter {
                        it.contains(changed.toString())
                    } as ArrayList<String>
                }
                adapter.notifyDataSetChanged()
            }
        }

        viewBinding.searchFrame.editTextTextPersonName.setOnEditorActionListener {
                textView, actionID, keyEvent ->
            when (actionID) {
                EditorInfo.IME_ACTION_DONE -> {
                    adapter.dataSet = ArrayList(full_data.filter {
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

    fun checkAlreadyCached(dataset: List<String>?, s: String): Boolean {
        if (dataset != null) {
            for (cache in dataset) {
                if (cache == "All languages") {
                    return false
                } else if (s == cache) {
                    return true
                }
            }
        }
        return false
    }

    fun checkRestrictedChoices(s: String): Boolean {
        return listOf("All languages", "Recently used").contains(s)
    }

    fun pickMostUsedLanguage(elementToAdd: String, position: Int, add_index: Int = 3) {
        val real_length = full_data.slice(1..add_index).takeWhile { it != "All languages" }.size
        val mostUsed = full_data.slice(1..real_length)

        if (adapter.added_size == add_index && elementToAdd !in mostUsed) {
            full_data.removeAt(add_index)
            adapter.notifyItemRemoved(add_index)
            adapter.added_size--
        }

        if (elementToAdd !in mostUsed) {
            full_data.add(1, elementToAdd)
            adapter.notifyItemInserted(1)
            adapter.added_size++
        } else {
            full_data[1] = elementToAdd
            adapter.notifyItemChanged(1)
        }

        var position = 2
        mostUsed.map {
            if (it != elementToAdd && position <= real_length) {
                full_data[position] = it
                adapter.notifyItemChanged(position)
                position++
            }
        }

        val resultIntent = Intent(this@LanguageActivity, MainActivity::class.java)
        resultIntent.putExtra("language", elementToAdd)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        val prefix = filesDir

        val languageData = DummyLanguageAdapter(full_data, adapter.base_size, adapter.added_size)
        writeFile("$prefix/languagedata.json", languageData)
    }

    override fun onItemClick(view: View?, position: Int) {
        val elementToAdd = adapter.getItem(position)
        if (checkRestrictedChoices(elementToAdd)) {
            return
        }

        Toast.makeText(
            this,
            "You clicked " + elementToAdd + " on row number " + position,
            Toast.LENGTH_SHORT
        ).show()

        pickMostUsedLanguage(elementToAdd, position)
    }
}