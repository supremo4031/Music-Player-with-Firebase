package com.arpan.myspotify.data.entities

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Song (
     @SerializedName("mediaId")
     val mediaId : String = "",

     @SerializedName("mediaUrl")
     val mediaUrl : String = "",

     @SerializedName("title")
     val title : String = "",

     @SerializedName("subtitle")
     val subtitle : String = "",

     @SerializedName("imageUrl")
     val imageUrl : String = ""
 ) : Serializable