package com.srivastava.teamplanner.models

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter.writeStringList

data class Card (
        val name: String = "",
        val createdBy: String = "",
        val assignedTo: ArrayList<String> = ArrayList(),
        val labelColor: String = ""

): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.createStringArrayList()!!,
            parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int)= with(parcel) {
        parcel.writeString(name)
        parcel.writeString(createdBy)
        parcel.writeStringList(assignedTo)
        parcel.writeString(labelColor)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card(parcel)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }
}