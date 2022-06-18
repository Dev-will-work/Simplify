package com.example.coursework

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.os.Parcel
import android.os.Parcelable
import android.widget.ImageButton
import kotlinx.serialization.Serializable

/**
 * Class, representing one previous user request. Also implements Parcelable interface.
 *
 * @property date
 * Timestamp of this previous request
 * @property request
 * Input text of the request
 * @property response
 * Simplified text of the request
 * @property is_favourite
 * Bool, representing is this request pinned or not.
 */
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

    /**
     * Function which writes data to Parcelable object.
     *
     * @param parcel
     * Input parcel instance.
     * @param flags
     * Special flags for additional manipulations.
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(date)
        parcel.writeString(request)
        parcel.writeString(response)
        parcel.writeByte(if (is_favourite) 1 else 0)
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
    companion object CREATOR : Parcelable.Creator<HistoryData> {
        override fun createFromParcel(parcel: Parcel): HistoryData {
            return HistoryData(parcel)
        }

        override fun newArray(size: Int): Array<HistoryData?> {
            return arrayOfNulls(size)
        }
    }
}


/**
 * Adapter class for manipulations with history list.
 *
 * @property dataSet
 * Raw data of previous requests, which will be rendered in the list.
 * @property FAVOURITE
 * Constant, representing that selected list item is important for user.
 * @property SIMPLE
 * Constant, representing that selected list item is not important for user.
 * @property clipboardManager
 * Object, that handles copy and paste operations.
 */
class HistoryAdapter(
    var dataSet: ArrayList<HistoryData>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>(), Parcelable {
    private val FAVOURITE = 0
    private val SIMPLE = 1
    private lateinit var clipboardManager: ClipboardManager

    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(HistoryData) as ArrayList<HistoryData>
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
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.date)
        val request: MyEditText = view.findViewById(R.id.request)
        val response: MyEditText = view.findViewById(R.id.response)
        val icon: ImageButton = view.findViewById(R.id.icon)
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
            .inflate(R.layout.history_item, viewGroup, false)

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

    /**
     * Returns the size of your dataset (invoked by the layout manager)
     *
     * @return size of list data.
     */
    override fun getItemCount() = dataSet.size

    /**
     * Function, that returns type of selected list item.
     *
     * @param position
     * position of item, which type we need to get.
     * @return Integer, representing type of list item.
     */
    override fun getItemViewType(position: Int): Int {
        return if (dataSet[position].is_favourite) {
            FAVOURITE
        } else {
            SIMPLE
        }
    }

    /**
     * Function, that puts instance of clipboard manager into adapter.
     *
     * @param clipboard
     * Instance of ClipboardManager to put in adapter.
     */
    fun setClipboard(clipboard: ClipboardManager) {
        clipboardManager = clipboard
    }

    /**
     * Function for random access to previous requests list.
     *
     * @param id
     * Number of previous request.
     * @return previous request with selected id.
     */
    fun getItem(id: Int): HistoryData {
        return dataSet[id]
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
        parcel.writeTypedList(dataSet)
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
    companion object CREATOR : Parcelable.Creator<HistoryAdapter> {
        override fun createFromParcel(parcel: Parcel): HistoryAdapter {
            return HistoryAdapter(parcel)
        }

        override fun newArray(size: Int): Array<HistoryAdapter?> {
            return arrayOfNulls(size)
        }
    }

}

/**
 * Util class for serialization of old user requests data.
 *
 * @property dataset
 * Serializable data of previous requests.
 */
@Serializable
data class DummyHistoryAdapter(val dataset: ArrayList<HistoryData>)

/**
 * Object that provides data of previous user requests to simplify text
 * to another components.
 * Compliant to SharedObject interface.
 * @property dataset
 * Data of previous requests.
 */
object HistoryAdapterObject : SharedObject<DummyHistoryAdapter> {
    lateinit var dataset: ArrayList<HistoryData>

    /**
     * Function which tells if this object is ready to use.
     * @receiver
     * returns true if previous requests data is initialized.
     *
     * @return Bool, that determines if this object properties are fully initialized and ready.
     */
    override fun initialized(): Boolean {
        return ::dataset.isInitialized
    }

    /**
     * This function sets deserialized data to object for further use.
     * @receiver
     * sets all previous requests data from deserialized object.
     *
     * @param ctx
     * Context of the application.
     * @param dummy
     * Class of the similar structure, needed for serialization.
     */
    override fun set(ctx: Context, dummy: DummyHistoryAdapter) {
        dataset = dummy.dataset
    }

    /**
     * Function for setting default values if the last state is missing.
     * @receiver
     * Sets empty arrayList.
     *
     * @param ctx
     * Context of the application.
     */
    override fun defaultInitialization(ctx: Context) {
        dataset = arrayListOf()
    }
}