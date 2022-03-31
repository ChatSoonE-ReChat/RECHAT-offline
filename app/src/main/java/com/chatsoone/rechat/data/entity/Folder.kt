package com.chatsoone.rechat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

// folderImg: Int -> String (using Bitmap) 변환 필요
// 폴더 정보를 담은 데이터
@Entity(tableName = "FolderTable")
data class Folder(
    @SerializedName("kakaoUserIdx") var kakaoUserIdx: Long = 0,
    @SerializedName("folderName") var folderName: String,
    @SerializedName("folderImg") var folderImg: Int?
):Serializable{
    @PrimaryKey(autoGenerate = true) @SerializedName("folderIdx") var idx: Int = 0
    @SerializedName("status") var status: String="ACTIVE"
    @SerializedName("parentFolderIdx") var parentFolderIdx: Int?=0
}