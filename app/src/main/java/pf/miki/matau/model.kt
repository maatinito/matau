package pf.miki.matau

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.util.DiffUtil


data class Ad(val id: String, val source : String, val title: String, val euroPrice: Float, val fcpPrice: Int, val vignette: String = "") : Parcelable {

    var liveDescription = MutableLiveData<String>()
    var liveDate = MutableLiveData<String>()
    var liveContact = MutableLiveData<String>()
    var liveLocation = MutableLiveData<String>()

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readFloat(),
            parcel.readInt(),
            parcel.readString()) {
        liveDescription.value = parcel.readString()
        liveDate.value = parcel.readString()
        liveContact.value = parcel.readString()
        liveLocation.value = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(source)
        parcel.writeString(title)
        parcel.writeFloat(euroPrice)
        parcel.writeInt(fcpPrice)
        parcel.writeString(vignette)
        parcel.writeString(liveDescription.value)
        parcel.writeString(liveDate.value)
        parcel.writeString(liveContact.value)
        parcel.writeString(liveLocation.value)
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