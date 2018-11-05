package pf.miki.matau.repository

import android.arch.lifecycle.MutableLiveData
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.util.DiffUtil
import java.util.*


data class Ad(var source: String, var id: String, var title: String, var fcpPrice: Int, var vignette: String = "") : Parcelable {

    var pinned = false
    var lastViewed = Date()
    var liveDescription = MutableLiveData<String>()
    var liveDate = MutableLiveData<Date>()
    var liveContact = MutableLiveData<String>()
    var liveLocation = MutableLiveData<String>()
    var liveImages = MutableLiveData<List<String>>()

    val euroPrice: Float
        get() = fcpPrice / 119.33174f

    constructor(s: String) : this(s, "0", "", 0, "")

    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readInt(),
            parcel.readString()!!) {
        pinned = parcel.readInt() != 0
        lastViewed = Date(parcel.readLong())
        liveDescription.value = parcel.readString()
        liveDate.value = Date(parcel.readLong())
        liveContact.value = parcel.readString()
        liveLocation.value = parcel.readString()
        liveImages.value = parcel.createStringArrayList()

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(source)
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeInt(fcpPrice)
        parcel.writeString(vignette)
        parcel.writeInt(if (pinned) 1 else 0 )
        parcel.writeLong(lastViewed.time)
        parcel.writeString(liveDescription.value)
        parcel.writeLong(liveDate.value?.time ?: Date().time)
        parcel.writeString(liveContact.value)
        parcel.writeString(liveLocation.value)
        parcel.writeStringList(liveImages.value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Ad> {

        override fun createFromParcel(parcel: Parcel): Ad {
            return Ad(parcel)
        }

        override fun newArray(size: Int): Array<Ad?> {
            return arrayOfNulls(size)
        }
    }

}
