package com.arpan.myspotify.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.arpan.myspotify.data.entities.Song

fun MediaMetadataCompat.toSong() : Song? {
    return description?.let {
        Song(
                it.mediaId ?: "",
                it.mediaUri.toString(),
                it.title.toString(),
                it.subtitle.toString(),
                it.iconUri.toString()
        )
    }
}