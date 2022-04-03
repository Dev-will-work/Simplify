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
import kotlinx.serialization.Serializable

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
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.textView)
            val text = textView.text
            view.setOnClickListener(this)
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

    private fun configure1(vh: ViewHolder, position: Int) {
        val param = vh.textView.layoutParams as ViewGroup.MarginLayoutParams
        val dpRatio: Float = vh.textView.context.getResources().getDisplayMetrics().density
        val pixelForDp = (38 * dpRatio).toInt()
        param.topMargin = pixelForDp
        vh.textView.layoutParams = param
        vh.textView.setTextColor(ContextCompat.getColor(vh.textView.context, R.color.base_500))
        vh.textView.setCompoundDrawablesWithIntrinsicBounds(android.R.color.transparent, 0, 0, 0)
    }

    private fun configure2(vh: ViewHolder, position: Int) {
        vh.textView.setTextColor(ContextCompat.getColor(vh.textView.context, R.color.base_500))
        vh.textView.setCompoundDrawablesWithIntrinsicBounds(android.R.color.transparent, 0, 0, 0)
    }

    private fun configure3(vh: ViewHolder, position: Int) {
        var endDrawable = 0
        if (vh.textView.text.equals(currentLanguage)) {
            endDrawable = R.drawable.ic_check
        }
        vh.textView.setCompoundDrawablesWithIntrinsicBounds(android.R.color.transparent, 0, endDrawable, 0)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val vh1 = viewHolder
        viewHolder.textView.text = dataSet[position]
        when (viewHolder.itemViewType) {
            HEADER_DOWN -> {
                configure1(vh1, position)
            }
            HEADER -> {
                configure2(vh1, position)
            }
            SIMPLE -> {
                configure3(vh1, position)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    override fun getItemViewType(position: Int): Int {
        if (position == 0 && dataSet[position].contains(' ')) {
            return HEADER
        } else if (dataSet[position].contains(' ')) {
            return HEADER_DOWN
        } else {
            return SIMPLE
        }
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    fun getItem(id: Int): String {
        return dataSet.get(id)
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(dataSet)
        parcel.writeInt(base_size)
        parcel.writeInt(added_size)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LanguageAdapter> {
        override fun createFromParcel(parcel: Parcel): LanguageAdapter {
            return LanguageAdapter(parcel)
        }

        override fun newArray(size: Int): Array<LanguageAdapter?> {
            return arrayOfNulls(size)
        }
    }

    fun setCurrentLanguage(language: String) {
        this.currentLanguage = language
    }

}

@Serializable
data class DummyLanguageAdapter(val dataset: ArrayList<String>,
                                val base_size: Int,
                                val added_size: Int)

object LanguageObject : SharedObject<DummyLanguageAdapter> {
    lateinit var dataset: ArrayList<String>
    var base_size: Int = 24
    var added_size: Int = 0
    override fun defaultInitialization(ctx: Context) {
        dataset = arrayListOf("Recently used", "All languages", "Arabic", "Bulgarian", "Catalan", "Czech", "Danish", "Dutch",
            "English", "Finnish", "French", "German", "Hungarian", "Indonesian", "Italian",
            "Norwegian", "Polish", "Portuguese", "Romanian", "Russian", "Spanish", "Swedish",
            "Turkish", "Ukrainian")
        added_size = 0
        base_size = 24

    }

    override fun set(ctx: Context, dummy: DummyLanguageAdapter) {
        this.base_size = dummy.base_size
        this.added_size = dummy.added_size
        this.dataset = dummy.dataset
    }

    override fun initialized(): Boolean {
        return this::dataset.isInitialized
    }
}