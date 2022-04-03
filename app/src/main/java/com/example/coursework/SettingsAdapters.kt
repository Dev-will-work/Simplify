package com.example.coursework

import android.content.ClipboardManager
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.data.model.CachedUser
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.serialization.Serializable
import kotlin.properties.Delegates

@Serializable
data class ToggleOptionsData(
    val option_name: String,
    var toggle_state: Boolean
)

class ToggleOptionsAdapter :
    RecyclerView.Adapter<ToggleOptionsAdapter.ViewHolder>() {
    private var mClickListener: ItemClickListener? = null
    lateinit var clipboardManager: ClipboardManager

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val optionName: TextView = view.findViewById(R.id.option_name)
        val switch: SwitchMaterial = view.findViewById(R.id.switch1)

        override fun onClick(view: View?) {
            mClickListener?.onItemClick(view, adapterPosition)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.toggle_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.optionName.text = SettingsObject.toggleData[position].option_name

        if (SettingsObject.toggleData[position].toggle_state) {
            viewHolder.switch.toggle()
        }

        if (SettingsObject.toggleData[position].option_name.contains("Improve") && SettingsObject.toggleData[position].toggle_state) {
            viewHolder.switch.setOnCheckedChangeListener { _, is_checked ->
                val useEmbeddingsRequest = if (!is_checked) {
                    "_without_embeddings"
                } else {
                    "_with_embeddings"
                }
                retrofitRequest(
                    viewHolder.switch.context,
                    useEmbeddingsRequest,
                    CachedUser.retrieveID(),
                    null,
                    true
                )
            }
        } else {
            viewHolder.switch.setOnCheckedChangeListener { compoundButton, is_checked ->
                SettingsObject.toggleData[position].toggle_state = is_checked
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = SettingsObject.toggleData.size

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    fun setClipboard(clipboard: ClipboardManager) {
        clipboardManager = clipboard
    }

    fun getItem(id: Int): ToggleOptionsData {
        return SettingsObject.toggleData.get(id)
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}


class SimpleOptionsAdapter(
    var dataSet: ArrayList<String>) :
    RecyclerView.Adapter<SimpleOptionsAdapter.ViewHolder>() {
    private var mClickListener: ItemClickListener? = null
    lateinit var clipboardManager: ClipboardManager

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val option_name: TextView

        init {
            option_name = view.findViewById(R.id.textView)

            // Define click listener for the ViewHolder's View.
            // view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            mClickListener?.onItemClick(view, adapterPosition)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.option_name.text = dataSet[position]
        viewHolder.option_name.setCompoundDrawables(null,null,null,null)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    fun setClipboard(clipboard: ClipboardManager) {
        clipboardManager = clipboard
    }

    fun getItem(id: Int): String {
        return dataSet.get(id)
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}

@Serializable
data class DummySettingsAdapters(
        var toggleData: ArrayList<ToggleOptionsData>,
        var simpleData: ArrayList<String>,
        var greeting: String)

object SettingsObject : SharedObject<DummySettingsAdapters> {
    lateinit var toggleData: ArrayList<ToggleOptionsData>
    lateinit var simpleData: ArrayList<String>
    lateinit var greeting: String

    override fun initialized(): Boolean {
        return this::toggleData.isInitialized && this::simpleData.isInitialized && this::greeting.isInitialized
    }

    override fun defaultInitialization(ctx: Context) {
        toggleData = arrayListOf(
            ToggleOptionsData("Autodetect language", false),
            ToggleOptionsData("Improve simplification", true),
            ToggleOptionsData("Disable input hints", false),
            ToggleOptionsData("Disable onboarding preview", false),
            ToggleOptionsData("Disable notifications", false),
            ToggleOptionsData("Disable greeting", true)
        )
        simpleData = arrayListOf("About us", "Feedback", "Help", "Rate our app")
        greeting = "Hello, User! Have a nice day!"
    }

    override fun set(ctx: Context, dummy: DummySettingsAdapters) {
        greeting = dummy.greeting
        toggleData = dummy.toggleData
        simpleData = dummy.simpleData
    }

    fun isPropertyToggled(partOptionName: String): Boolean {
        return this.toggleData.filter {
            it.option_name.contains(partOptionName)
        }[0].toggle_state
    }
}