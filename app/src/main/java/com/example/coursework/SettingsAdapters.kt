package com.example.coursework

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial

data class ToggleOptionsData(
    val option_name: String?,
    val toggle_state: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(option_name)
        parcel.writeByte(if (toggle_state) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ToggleOptionsData> {
        override fun createFromParcel(parcel: Parcel): ToggleOptionsData {
            return ToggleOptionsData(parcel)
        }

        override fun newArray(size: Int): Array<ToggleOptionsData?> {
            return arrayOfNulls(size)
        }
    }
}

class ToggleOptionsAdapter(
    var dataSet: ArrayList<ToggleOptionsData>) :
    RecyclerView.Adapter<ToggleOptionsAdapter.ViewHolder>(), Parcelable {
    private val FAVOURITE = 0
    private val SIMPLE = 1
    private var mClickListener: ItemClickListener? = null
    private var currentLanguage: String? = null
    lateinit var clipboardManager: ClipboardManager

    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(ToggleOptionsData) as ArrayList<ToggleOptionsData>
    ) {

    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val option_name: TextView
        val switch: SwitchMaterial

        init {
            option_name = view.findViewById(R.id.option_name)
            switch = view.findViewById(R.id.switch1)

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
        val viewHolder: RecyclerView.ViewHolder
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.toggle_item, viewGroup, false)

        return ViewHolder(view)
    }

//    private fun configure1(vh: ViewHolder, position: Int) {
//        val param = vh.textView.layoutParams as ViewGroup.MarginLayoutParams
//        val dpRatio: Float = vh.textView.context.getResources().getDisplayMetrics().density
//        val pixelForDp = (38 as Int * dpRatio).toInt()
//        param.topMargin = pixelForDp
//        vh.textView.layoutParams = param
//        vh.textView.setTextColor(ContextCompat.getColor(vh.textView.context, R.color.base_500))
//        vh.textView.setCompoundDrawablesWithIntrinsicBounds(android.R.color.transparent, 0, 0, 0);
//    }
//
//    private fun configure2(vh: ViewHolder, position: Int) {
//        vh.textView.setTextColor(ContextCompat.getColor(vh.textView.context, R.color.base_500))
//        vh.textView.setCompoundDrawablesWithIntrinsicBounds(android.R.color.transparent, 0, 0, 0);
//    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val vh1 = viewHolder as ViewHolder
        viewHolder.option_name.text = dataSet[position].option_name
//        viewHolder.icon.setImageResource(if (dataSet[position].is_favourite) R.drawable.ic_bookmark else R.drawable.ic_bookmark_outline)
//        when (viewHolder.itemViewType) {
//            FAVOURITE -> {
//                configure1(vh1, position)
//            }
//            SIMPLE -> {
//                configure2(vh1, position)
//            }
//        }
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

    fun getItem(id: Int): ToggleOptionsData {
        return dataSet.get(id)
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(dataSet)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HistoryAdapter> {
        override fun createFromParcel(parcel: Parcel): HistoryAdapter {
            return HistoryAdapter(parcel)
        }

        override fun newArray(size: Int): Array<HistoryAdapter?> {
            return arrayOfNulls(size)
        }
    }

    fun setCurrentLanguage(language: String) {
        this.currentLanguage = language
    }

}


class SimpleOptionsAdapter(
    var dataSet: ArrayList<String>) :
    RecyclerView.Adapter<SimpleOptionsAdapter.ViewHolder>(), Parcelable {
    private val FAVOURITE = 0
    private val SIMPLE = 1
    private var mClickListener: ItemClickListener? = null
    private var currentLanguage: String? = null
    lateinit var clipboardManager: ClipboardManager

    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList() as ArrayList<String>
    ) {

    }

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
        val viewHolder: RecyclerView.ViewHolder
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item, viewGroup, false)

        return ViewHolder(view)
    }

//    private fun configure1(vh: ViewHolder, position: Int) {
//        val param = vh.textView.layoutParams as ViewGroup.MarginLayoutParams
//        val dpRatio: Float = vh.textView.context.getResources().getDisplayMetrics().density
//        val pixelForDp = (38 as Int * dpRatio).toInt()
//        param.topMargin = pixelForDp
//        vh.textView.layoutParams = param
//        vh.textView.setTextColor(ContextCompat.getColor(vh.textView.context, R.color.base_500))
//        vh.textView.setCompoundDrawablesWithIntrinsicBounds(android.R.color.transparent, 0, 0, 0);
//    }
//
//    private fun configure2(vh: ViewHolder, position: Int) {
//        vh.textView.setTextColor(ContextCompat.getColor(vh.textView.context, R.color.base_500))
//        vh.textView.setCompoundDrawablesWithIntrinsicBounds(android.R.color.transparent, 0, 0, 0);
//    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val vh1 = viewHolder as ViewHolder
        viewHolder.option_name.text = dataSet[position]
        viewHolder.option_name.setCompoundDrawables(null,null,null,null)

//        viewHolder.icon.setOnClickListener {
//            dataSet[position].is_favourite = dataSet[position].is_favourite.xor(true)
//            if (dataSet[position].is_favourite) {
//                viewHolder.icon.setImageResource(R.drawable.ic_bookmark)
//            } else {
//                viewHolder.icon.setImageResource(R.drawable.ic_bookmark_outline)
//            }
//            dataSet.sortByDescending {
//                it.is_favourite
//            }
//            notifyDataSetChanged()
//        }
//        when (viewHolder.itemViewType) {
//            FAVOURITE -> {
//                configure1(vh1, position)
//            }
//            SIMPLE -> {
//                configure2(vh1, position)
//            }
//        }
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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(dataSet)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HistoryAdapter> {
        override fun createFromParcel(parcel: Parcel): HistoryAdapter {
            return HistoryAdapter(parcel)
        }

        override fun newArray(size: Int): Array<HistoryAdapter?> {
            return arrayOfNulls(size)
        }
    }

    fun setCurrentLanguage(language: String) {
        this.currentLanguage = language
    }

}