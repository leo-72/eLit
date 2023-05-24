package com.android.elit.dataclass

import android.os.Parcel
import android.os.Parcelable

data class Books(
    var id: String? = null,
    var image: String? = null,
    var title: String? = null,
    var author: String? = null,
    var description: String? = null,
    var genre: String? = null,
    var pdfUrl: String? = null
    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, item: Int) {
        parcel.writeString(id)
        parcel.writeString(image)
        parcel.writeString(title)
        parcel.writeString(author)
        parcel.writeString(description)
        parcel.writeString(genre)
        parcel.writeString(pdfUrl)
    }

    companion object CREATOR : Parcelable.Creator<Books> {
        override fun createFromParcel(parcel: Parcel): Books {
            return Books(parcel)
        }

        override fun newArray(size: Int): Array<Books?> {
            return arrayOfNulls(size)
        }
    }
}
