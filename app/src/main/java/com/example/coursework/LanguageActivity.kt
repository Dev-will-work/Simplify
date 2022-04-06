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
import java.util.ArrayList


class LanguageActivity : AppCompatActivity(), LanguageAdapter.ItemClickListener {
    private lateinit var adapter: LanguageAdapter
    private lateinit var viewBinding: ActivityLanguageBinding
    private lateinit var fullData: ArrayList<String>

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

        fullData = adapter.dataSet

        viewBinding.searchFrame.editTextTextPersonName.addTextChangedListener {
                changed ->
            if (changed != null) {
                if (changed.toString() == "") {
                    adapter.dataSet = fullData
                } else {
                    adapter.dataSet = fullData.filter {
                        it.contains(changed.toString())
                    } as ArrayList<String>
                }
                adapter.notifyDataSetChanged()
            }
        }

        viewBinding.searchFrame.editTextTextPersonName.setOnEditorActionListener {
                _, actionID, _ ->
            when (actionID) {
                EditorInfo.IME_ACTION_DONE -> {
                    adapter.dataSet = ArrayList(fullData.filter {
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

    private fun checkRestrictedChoices(s: String): Boolean {
        return listOf("All languages", "Recently used").contains(s)
    }

    private fun pickMostUsedLanguage(elementToAdd: String, position: Int, add_index: Int = 3) {
        val realLength = fullData.slice(1..add_index).takeWhile { it != "All languages" }.size
        val mostUsed = fullData.slice(1..realLength)

        if (adapter.added_size == add_index && elementToAdd !in mostUsed) {
            fullData.removeAt(add_index)
            adapter.notifyItemRemoved(add_index)
            adapter.added_size--
        }

        if (elementToAdd !in mostUsed) {
            fullData.add(1, elementToAdd)
            adapter.notifyItemInserted(1)
            adapter.added_size++
        } else {
            fullData[1] = elementToAdd
            adapter.notifyItemChanged(1)
        }

        var changingPosition = 2
        mostUsed.map {
            if (it != elementToAdd && changingPosition <= realLength) {
                fullData[changingPosition] = it
                adapter.notifyItemChanged(changingPosition)
                changingPosition++
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

        val languageData = DummyLanguageAdapter(fullData, adapter.base_size, adapter.added_size)
        writeFile("$prefix/languagedata.json", languageData)
    }

    override fun onItemClick(view: View?, position: Int) {
        val elementToAdd = adapter.getItem(position)
        if (checkRestrictedChoices(elementToAdd)) {
            return
        }

        Toast.makeText(
            this,
            "You clicked $elementToAdd on row number $position",
            Toast.LENGTH_SHORT
        ).show()

        pickMostUsedLanguage(elementToAdd, position)
    }
}