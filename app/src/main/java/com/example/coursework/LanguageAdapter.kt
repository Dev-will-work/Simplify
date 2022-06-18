package com.example.coursework

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.os.Parcel
import android.os.Parcelable
import com.example.coursework.ImageStore.image_uri
import kotlinx.serialization.Serializable

/**
 * Adapter class for manipulations with language list.
 *
 * @property dataSet
 * ArrayList with languages in adapter.
 * @property base_size
 * stable amount of languages in not ranked part.
 * @property added_size
 * amount of languages in ranked part.
 * @property HEADER_DOWN
 * Integer, representing header with top margin.
 * @property HEADER
 * Integer, representing Header.
 * @property SIMPLE
 * Integer, representing simple language list item.
 * @property mClickListener
 * Object, responsive for clicks on languages.
 * @property currentLanguage
 * last selected language, english by default.
 */
class LanguageAdapter(
    var dataSet: ArrayList<String>,
    var base_size: Int = dataSet.size,
    var added_size: Int = 0) :
    RecyclerView.Adapter<LanguageAdapter.ViewHolder>(), Parcelable {
    private val HEADER_DOWN = 0
    private val HEADER = 1
    private val SIMPLE = 2
    private var mClickListener: ItemClickListener? = null
    private var currentLanguage: String? = null

    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList() as ArrayList<String>,
        parcel.readInt(),
        parcel.readInt()
    )

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     *
     * @constructor
     * Initializes all fields of list item.
     *
     * @param view
     * View, that will be injected into this Viewholder.
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val textView: TextView = view.findViewById(R.id.textView)

        init {
            // Define click listener for the ViewHolder's View.
            view.setOnClickListener(this)
        }

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
    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item, viewGroup, false)

        return ViewHolder(view)
    }

    /**
     * Function for personal settings for header list item with top margin.
     *
     * @param vh
     * related viewholder with view.
     * @param position
     * position of current list item.
     */
    private fun configure1(vh: ViewHolder, position: Int) {
        val param = vh.textView.layoutParams as ViewGroup.MarginLayoutParams
        val dpRatio: Float = vh.textView.context.resources.displayMetrics.density
        val pixelForDp = (38 * dpRatio).toInt()
        param.topMargin = pixelForDp
        vh.textView.layoutParams = param
        vh.textView.setTextColor(ContextCompat.getColor(vh.textView.context, R.color.base_500))
        vh.textView.setCompoundDrawablesWithIntrinsicBounds(android.R.color.transparent, 0, 0, 0)
    }

    /**
     * Function for personal settings for header list item.
     *
     * @param vh
     * related viewholder with view.
     * @param position
     * position of current list item.
     */
    private fun configure2(vh: ViewHolder, position: Int) {
        vh.textView.setTextColor(ContextCompat.getColor(vh.textView.context, R.color.base_500))
        vh.textView.setCompoundDrawablesWithIntrinsicBounds(android.R.color.transparent, 0, 0, 0)
    }

    /**
     * Function for personal settings for language list item.
     *
     * @param vh
     * related viewholder with view.
     * @param position
     * position of current list item.
     */
    private fun configure3(vh: ViewHolder, position: Int) {
        var endDrawable = 0
        if (vh.textView.text.equals(currentLanguage)) {
            endDrawable = R.drawable.ic_check
        }
        vh.textView.setCompoundDrawablesWithIntrinsicBounds(android.R.color.transparent, 0, endDrawable, 0)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     *
     * @param viewHolder
     * abstract object, representing one list item.
     * @param position
     * position of the corresponding list item.
     */
    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position]
        when (viewHolder.itemViewType) {
            HEADER_DOWN -> {
                configure1(viewHolder, position)
            }
            HEADER -> {
                configure2(viewHolder, position)
            }
            SIMPLE -> {
                configure3(viewHolder, position)
            }
        }
    }

    /**
     * Returns the size of your dataset (invoked by the layout manager)
     *
     * @return size of list data.
     */
    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    /**
     * Function, that returns type of selected list item.
     *
     * @param position
     * position of item, which type we need to get.
     * @return Integer, representing type of list item.
     */
    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && dataSet[position].contains(' ')) {
            HEADER
        } else if (dataSet[position].contains(' ')) {
            HEADER_DOWN
        } else {
            SIMPLE
        }
    }

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
     * Function for random access to language list.
     *
     * @param id
     * Number of language.
     * @return language with selected id.
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

    /**
     * Function which writes data to Parcelable object.
     *
     * @param parcel
     * Input parcel instance.
     * @param flags
     * Special flags for additional manipulations.
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(dataSet)
        parcel.writeInt(base_size)
        parcel.writeInt(added_size)
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable instance's
     * marshaled representation.
     * @return
     * Number, representing Parcelable contents.
     */
    override fun describeContents(): Int {
        return 0
    }

    /**
     * Special companion object, that generates Parcelable instances.
     */
    companion object CREATOR : Parcelable.Creator<LanguageAdapter> {
        override fun createFromParcel(parcel: Parcel): LanguageAdapter {
            return LanguageAdapter(parcel)
        }

        override fun newArray(size: Int): Array<LanguageAdapter?> {
            return arrayOfNulls(size)
        }
    }

    /**
     * Setter of the current language.
     *
     * @param language
     * language to set as current(visible in UI).
     */
    fun setCurrentLanguage(language: String) {
        this.currentLanguage = language
    }

}

/**
 *
 * Util class for serialization of language data.
 *
 * @property dataset
 * ArrayList with languages.
 * @property base_size
 * stable amount of languages in not ranked part.
 * @property added_size
 * amount of languages in ranked part.
 */
@Serializable
data class DummyLanguageAdapter(val dataset: ArrayList<String>,
                                val base_size: Int,
                                val added_size: Int)

/**
 * Object that provides languages for simplification to another components.
 * Compliant to SharedObject interface.
 * @property dataset
 * ArrayList with languages.
 * @property base_size
 * stable amount of languages in not ranked part.
 * @property added_size
 * amount of languages in ranked part.
 */
object LanguageObject : SharedObject<DummyLanguageAdapter> {
    lateinit var dataset: ArrayList<String>
    var base_size: Int = 24
    var added_size: Int = 0

    /**
     * Function for setting default values if the last state is missing.
     * @receiver
     * sets languages to default hardcoded list from MILES documentation, added size to 0
     * and base_size to constant language size.
     *
     * @param ctx
     * Context of the application.
     */
    override fun defaultInitialization(ctx: Context) {
        dataset = arrayListOf("Recently used", "All languages", "Arabic", "Bulgarian", "Catalan", "Czech", "Danish", "Dutch",
            "English", "Finnish", "French", "German", "Hungarian", "Indonesian", "Italian",
            "Norwegian", "Polish", "Portuguese", "Romanian", "Russian", "Spanish", "Swedish",
            "Turkish", "Ukrainian")
        added_size = 0
        base_size = 24

    }

    /**
     * This function sets deserialized data to object for further use.
     * @receiver
     * Sets language list sizes and language data from dummy to provider object.
     *
     * @param ctx
     * Context of the application.
     * @param dummy
     * Class of the similar structure, needed for serialization.
     */
    override fun set(ctx: Context, dummy: DummyLanguageAdapter) {
        this.base_size = dummy.base_size
        this.added_size = dummy.added_size
        this.dataset = dummy.dataset
    }

    /**
     * Function which tells if this object is ready to use.
     * @receiver
     * returns true if language data is initialized.
     *
     * @return Bool, that determines if this object properties are fully initialized and ready.
     *
     */
    override fun initialized(): Boolean {
        return this::dataset.isInitialized
    }
}