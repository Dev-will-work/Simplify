package com.example.coursework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityLanguageBinding

import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import java.util.ArrayList

/**
 * Class, that handles language screen and list of previous requests.
 * @property viewBinding
 * Util object, that simplifies access to activity parts.
 * @property adapter
 * Adapter class for storing and rendering languages.
 * @property fullData
 * ArrayList with languages.
 *
 */
class LanguageActivity : AppCompatActivity(), LanguageAdapter.ItemClickListener {
    private lateinit var adapter: LanguageAdapter
    private lateinit var viewBinding: ActivityLanguageBinding
    private lateinit var fullData: ArrayList<String>

    /**
     * Function, executed when the application is opened first time.
     * @receiver
     * Setups language list and other interactive behaviour on the history screen.
     *
     * @param savedInstanceState
     * Bundle with simple types, can be used for temporal storage
     */
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

        while (SettingsObject.usedLanguages < adapter.added_size) {
            fullData.removeAt(adapter.added_size)
            adapter.notifyItemRemoved(adapter.added_size)
            adapter.added_size--
        }

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

    /**
     * Util function that checks if the language is already ranked.
     *
     * @param dataset
     * List of languages.
     * @param s
     * Language, that we check.
     * @return Bool, representing if language [s] is ranked or not.
     */
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

    /**
     * Util function, that checks if input string is definitely a language, instead of header.
     *
     * @param s
     * String with language to check.
     * @return true if string [s] is language, false if separator or header
     */
    private fun checkRestrictedChoices(s: String): Boolean {
        return listOf("All languages", "Recently used").contains(s)
    }

    /**
     * Function which adds language from the list to the ranked list part.
     *
     * @param elementToAdd
     * Language to add.
     * @param position
     * position of selected element.
     * @param add_index
     * Integer, representing index of element, where ranking part is ended.
     */
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

    /**
     * Lifecycle function, which executes when the app is out of focus.
     * @receiver
     * Serializes data from language list.
     *
     */
    override fun onPause() {
        super.onPause()
        val prefix = filesDir

        val languageData = DummyLanguageAdapter(fullData, adapter.base_size, adapter.added_size)
        LanguageObject.set(this, languageData)
        writeFile("$prefix/languagedata.json", languageData)
    }

    /**
     * Function, which adds selected language to ranked list part.
     *
     * @param view
     * Selected element casted to the View.
     * @param position
     * position of the element we clicked on.
     */
    override fun onItemClick(view: View?, position: Int) {
        val elementToAdd = adapter.getItem(position)
        if (checkRestrictedChoices(elementToAdd)) {
            return
        }

//        Toast.makeText(
//            this,
//            "You clicked $elementToAdd on row number $position",
//            Toast.LENGTH_SHORT
//        ).show()

        pickMostUsedLanguage(elementToAdd, position, SettingsObject.usedLanguages.toInt())
    }
}