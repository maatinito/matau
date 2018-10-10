package pf.miki.matau

import android.arch.lifecycle.MutableLiveData
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.util.DiffUtil


data class Ad(var source: String, var id: String, var title: String, var fcpPrice: Int, var vignette: String = "") : Parcelable {

    var liveDescription = MutableLiveData<String>()
    var liveDate = MutableLiveData<String>()
    var liveContact = MutableLiveData<String>()
    var liveLocation = MutableLiveData<String>()
    var liveImages = MutableLiveData<List<String>>()

    val euroPrice: Float
        get() = fcpPrice / 119.33174f

    constructor(s: String) : this(s, "0", "", 0, "")

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString()) {
        liveDescription.value = parcel.readString()
        liveDate.value = parcel.readString()
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
        parcel.writeString(liveDescription.value)
        parcel.writeString(liveDate.value)
        parcel.writeString(liveContact.value)
        parcel.writeString(liveLocation.value)
        parcel.writeStringList(liveImages.value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Ad> {
        val EqualCallBack: DiffUtil.ItemCallback<Ad> = object : DiffUtil.ItemCallback<Ad>() {
            override fun areItemsTheSame(oldItem: Ad, newItem: Ad): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Ad, newItem: Ad): Boolean {
                return oldItem.id == newItem.id
            }
        }

        override fun createFromParcel(parcel: Parcel): Ad {
            return Ad(parcel)
        }

        override fun newArray(size: Int): Array<Ad?> {
            return arrayOfNulls(size)
        }
    }

}