package com.android.elit.dataclass

import android.os.Parcel
import android.os.Parcelable

data class Users (
    val id : String? = null,
    val fullname : String? = null,
    val email : String? = null,
    val phone : String? = null,
    val username : String? = null,
    val password : String? = null,
    val user : String? = null,
    val role : String? = null,
    val isFav: List<String> = emptyList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(fullname)
        parcel.writeString(email)
        parcel.writeString(phone)
        parcel.writeString(username)
        parcel.writeString(password)
        parcel.writeString(user)
        parcel.writeString(role)
        parcel.writeStringList(isFav)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Users> {
        override fun createFromParcel(parcel: Parcel): Users {
            return Users(parcel)
        }

        override fun newArray(size: Int): Array<Users?> {
            return arrayOfNulls(size)
        }
    }

}