package kr.puze.autoparking

import android.os.Parcel
import android.os.Parcelable

class OutData(var carName: String?, var time: String?, var price: Int) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(carName)
        writeString(time)
        writeInt(price)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<OutData> = object : Parcelable.Creator<OutData> {
            override fun createFromParcel(source: Parcel): OutData = OutData(source)
            override fun newArray(size: Int): Array<OutData?> = arrayOfNulls(size)
        }
    }
}