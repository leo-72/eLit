package com.android.elit.dataclass

import android.os.Parcel
import android.os.Parcelable

data class FavUsers(
    val id: String? = null,
    val image: String? = null,
    val title: String? = null,
    val author: String? = null,
    val description: String? = null,
    val genre: String? = null,
    val pdfUrl: String? = null,
    val isFav: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(image)
        parcel.writeString(title)
        parcel.writeString(author)
        parcel.writeString(description)
        parcel.writeString(genre)
        parcel.writeString(pdfUrl)
        parcel.writeByte(if (isFav) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FavUsers> {
        override fun createFromParcel(parcel: Parcel): FavUsers {
            return FavUsers(parcel)
        }

        override fun newArray(size: Int): Array<FavUsers?> {
            return arrayOfNulls(size)
        }
    }
}