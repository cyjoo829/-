package com.example.hometraing.selected

import android.os.Parcel
import android.os.Parcelable

data class Exercise(
    val name: String?,
    val category: String?,
    val description: String?,
    val duration: String?, // 예: "30초", "60초"와 같은 형식
    val caloriesBurned: String?
) : Parcelable {

    // Parcelable 구현을 위한 생성자
    constructor(parcel: Parcel) : this(
        parcel.readString(), // name (String?)
        parcel.readString(), // category (String?)
        parcel.readString(), // description (String?)
        parcel.readString(), // duration (String?)
        parcel.readString()  // caloriesBurned (String?)
    )

    // 객체를 Parcel에 쓰는 메서드
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(category)
        parcel.writeString(description)
        parcel.writeString(duration)
        parcel.writeString(caloriesBurned)
    }

    // Parcelable 객체의 내용을 기술하는 메서드 (대부분 0 반환)
    override fun describeContents(): Int {
        return 0
    }

    // Parcelable 객체를 생성하는 데 사용되는 CREATOR 객체
    companion object CREATOR : Parcelable.Creator<Exercise> {
        // Parcel에서 Exercise 객체를 생성
        override fun createFromParcel(parcel: Parcel): Exercise {
            return Exercise(parcel)
        }

        // Exercise 객체 배열을 생성
        override fun newArray(size: Int): Array<Exercise?> {
            return arrayOfNulls(size)
        }
    }
}