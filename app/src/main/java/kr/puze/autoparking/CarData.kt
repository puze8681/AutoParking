package kr.puze.autoparking

import android.os.Parcel
import android.os.Parcelable

class CarData(var carName: String?, var time: Long) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(carName)
        writeSerializable(time)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CarData> = object : Parcelable.Creator<CarData> {
            override fun createFromParcel(source: Parcel): CarData = CarData(source)
            override fun newArray(size: Int): Array<CarData?> = arrayOfNulls(size)
        }
    }
}