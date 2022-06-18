package com.example.coursework

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.ImageStore.image_uri
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.serialization.Serializable

/**
 * Dummy class for one toggle option.
 *
 * @property option_name
 * Name of the toggle option.
 * @property toggle_state
 * State, true if the option is turned on or false if it is turned off.
 */
@Serializable
data class ToggleOptionsData(
    val option_name: String,
    var toggle_state: Boolean
)

/**
 * Adapter class for manipulations with toggle settings list.
 *
 * @property mClickListener
 * Listener object, responsible for handling clicks.
 */
class ToggleOptionsAdapter :
    RecyclerView.Adapter<ToggleOptionsAdapter.ViewHolder>() {
    private var mClickListener: ItemClickListener? = null

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     *
     * @constructor
     * Initializes all fields of list item.
     *
     * @param view
     * View, that will be injected into this ViewHolder.
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val optionName: TextView = view.findViewById(R.id.option_name)
        val switch: SwitchMaterial = view.findViewById(R.id.switch1)

        override fun onClick(view: View?) {
            mClickListener?.onItemClick(view, adapterPosition)
        }
    }

    /**
     * Create new views (invoked by the layout manager)
     *
     * @param viewGroup
     * parent object, containing necessary context.
     * @param viewType
     * @return Viewholder object containing view.
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.toggle_item, viewGroup, false)

        return ViewHolder(view)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     *
     * @param viewHolder
     * abstract object, representing one list item.
     * @param position
     * position of the corresponding list item.
     */
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
            viewHolder.switch.setOnCheckedChangeListener { _, is_checked ->
                SettingsObject.toggleData[position].toggle_state = is_checked
            }
        }
    }

    /**
     * Returns the size of your dataset (invoked by the layout manager)
     *
     * @return size of list data.
     */
    override fun getItemCount() = SettingsObject.toggleData.size

    /**
     * Function for random access to toggle settings list.
     *
     * @param id
     * Number of toggle setting.
     * @return previous setting with selected id.
     */
    fun getItem(id: Int): ToggleOptionsData {
        return SettingsObject.toggleData[id]
    }

    /**
     * Helper interface to process list item clicks.
     *
     */
    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}

/**
 * Adapter class for manipulations with list of simple clickable options.
 *
 * @property dataSet
 * ArrayList with clickable settings in adapter.
 *
 * @property mClickListener
 * Listener object, responsible for handling clicks.
 */
class SimpleOptionsAdapter(
    var dataSet: ArrayList<String>) :
    RecyclerView.Adapter<SimpleOptionsAdapter.ViewHolder>() {
    private var mClickListener: ItemClickListener? = null

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     *
     * @constructor
     * Initializes all fields of list item.
     *
     * @param view
     * View, that will be injected into this ViewHolder.
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val optionName: TextView = view.findViewById(R.id.textView)

        override fun onClick(view: View?) {
            mClickListener?.onItemClick(view, adapterPosition)
        }
    }

    /**
     * Create new views (invoked by the layout manager)
     *
     * @param viewGroup
     * parent object, containing necessary context.
     * @param viewType
     * @return Viewholder object containing view.
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item, viewGroup, false)

        return ViewHolder(view)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     *
     * @param viewHolder
     * abstract object, representing one list item.
     * @param position
     * position of the corresponding list item.
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.optionName.text = dataSet[position]
        viewHolder.optionName.setCompoundDrawables(null,null,null,null)
    }

    /**
     * Returns the size of your dataset (invoked by the layout manager)
     *
     * @return size of list data.
     */
    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    /**
     * Function which sets click listener into adapter.
     *
     * @param itemClickListener
     * External click listener to put in adapter.
     */
    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    /**
     * Function for random access to clickable settings list.
     *
     * @param id
     * Number of clickable setting.
     * @return previous setting with selected id.
     */
    fun getItem(id: Int): String {
        return dataSet[id]
    }

    /**
     * Helper interface to process list item clicks.
     *
     */
    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}

/**
 * TODO
 *
 * @property toggleData
 * @property simpleData
 * @property greeting
 * @property usedLanguages
 */
@Serializable
data class DummySettingsAdapters(
        var toggleData: ArrayList<ToggleOptionsData>,
        var simpleData: ArrayList<String>,
        var greeting: String,
        var usedLanguages: Float)

/**
 * Object that provides greeting and settings data to another components.
 * Compliant to SharedObject interface.
 * @property toggleData
 * ArrayList of data for toggle options.
 * @property simpleData
 * ArrayList of data for simple clickable options.
 * @property greeting
 * User greeting contents.
 * @property usedLanguages
 * Number, representing the size of ranking part in language list.
 */
object SettingsObject : SharedObject<DummySettingsAdapters> {
    lateinit var toggleData: ArrayList<ToggleOptionsData>
    lateinit var simpleData: ArrayList<String>
    lateinit var greeting: String
    var usedLanguages: Float = 0.0f

    /**
     * Function which tells if this object is ready to use.
     * @receiver
     * returns true if all adapter data and greeting are initialized.
     *
     * @return Bool, that determines if this object properties are fully initialized and ready.
     *
     */
    override fun initialized(): Boolean {
        return this::toggleData.isInitialized && this::simpleData.isInitialized && this::greeting.isInitialized
    }

    /**
     * Function for setting default values if the last state is missing.
     * @receiver
     * Fills ArrayLists of options with hardcoded lists of data and sets base user greeting,
     * default ranked languages size is 3.
     *
     * @param ctx
     * Context of the application.
     */
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
        usedLanguages = 3.0f
    }

    /**
     * This function sets deserialized data to object for further use.
     * @receiver
     * Sets adapters data, greeting data and ranked languages size from dummy to shared object.
     *
     * @param ctx
     * Context of the application.
     * @param dummy
     * Class of the similar structure, needed for serialization.
     */
    override fun set(ctx: Context, dummy: DummySettingsAdapters) {
        greeting = dummy.greeting
        toggleData = dummy.toggleData
        simpleData = dummy.simpleData
        usedLanguages = dummy.usedLanguages
    }

    /**
     * Helper function, which returns true if the option with [partOptionName] is toggled
     * or false otherwise.
     *
     * @param partOptionName
     * @return
     */
    fun isPropertyToggled(partOptionName: String): Boolean {
        return this.toggleData.filter {
            it.option_name.contains(partOptionName)
        }[0].toggle_state
    }
}