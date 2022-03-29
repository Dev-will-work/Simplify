package com.example.coursework

import android.content.ClipData
import android.content.ClipboardManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.os.Parcel
import android.os.Parcelable
import android.widget.ImageButton
import kotlinx.serialization.Serializable

@Serializable
data class HistoryData(
    val date: String?,
    val request: String?,
    val response: String?,
    var is_favourite: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(date)
        parcel.writeString(request)
        parcel.writeString(response)
        parcel.writeByte(if (is_favourite) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HistoryData> {
        override fun createFromParcel(parcel: Parcel): HistoryData {
            return HistoryData(parcel)
        }

        override fun newArray(size: Int): Array<HistoryData?> {
            return arrayOfNulls(size)
        }
    }
}


class HistoryAdapter(
    var dataSet: ArrayList<HistoryData>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>(), Parcelable {
    private val FAVOURITE = 0
    private val SIMPLE = 1
    lateinit var clipboardManager: ClipboardManager

    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(HistoryData) as ArrayList<HistoryData>
    )

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView
        val request: MyEditText
        val response: MyEditText
        val icon: ImageButton

        init {
            icon = view.findViewById(R.id.icon)
            date = view.findViewById(R.id.date)
            request = view.findViewById(R.id.request)
            response = view.findViewById(R.id.response)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.history_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.date.text = dataSet[position].date
        viewHolder.request.setText(dataSet[position].request)
        viewHolder.response.setText(dataSet[position].response)
        viewHolder.icon.setImageResource(if (dataSet[position].is_favourite) R.drawable.ic_bookmark else R.drawable.ic_bookmark_outline)

        viewHolder.request.setOnClickListener {
            val clip = ClipData.newPlainText(null, viewHolder.request.text.toString())
            clipboardManager.setPrimaryClip(clip)
        }

        viewHolder.date.setOnClickListener {
            val clip = ClipData.newPlainText(null, viewHolder.date.text.toString())
            clipboardManager.setPrimaryClip(clip)
        }

        viewHolder.response.setOnClickListener {
            val clip = ClipData.newPlainText(null, viewHolder.response.text.toString())
            clipboardManager.setPrimaryClip(clip)
        }

        viewHolder.icon.setOnClickListener {
            dataSet[position].is_favourite = dataSet[position].is_favourite.xor(true)
            if (dataSet[position].is_favourite) {
                viewHolder.icon.setImageResource(R.drawable.ic_bookmark)
            } else {
                viewHolder.icon.setImageResource(R.drawable.ic_bookmark_outline)
            }
            dataSet.sortByDescending {
                it.is_favourite
            }
            notifyDataSetChanged()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    override fun getItemViewType(position: Int): Int {
        if (dataSet[position].is_favourite) {
            return FAVOURITE
        } else {
            return SIMPLE
        }
    }

    fun setClipboard(clipboard: ClipboardManager) {
        clipboardManager = clipboard
    }

    fun getItem(id: Int): HistoryData {
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

}

@Serializable
data class DummyHistoryAdapter(val dataset: ArrayList<HistoryData>)

object HistoryAdapterObject {
    lateinit var dataset: ArrayList<HistoryData>
}