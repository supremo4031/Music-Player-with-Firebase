package com.arpan.myspotify.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.arpan.myspotify.R
import com.arpan.myspotify.data.entities.Song
import com.arpan.myspotify.exoplayer.isPlaying
import com.arpan.myspotify.exoplayer.toSong
import com.arpan.myspotify.other.Status
import com.arpan.myspotify.ui.viewmodels.MainViewModel
import com.arpan.myspotify.ui.viewmodels.SongViewModel
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide : RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel : SongViewModel by viewModels()

    private var curPlayingSong : Song? = null

    private var playbackState : PlaybackStateCompat? = null

    private var shouldUpdateSeekBar = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObServers()

        ivPlayPauseDetail.setOnClickListener{
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        ivSkipPrevious.setOnClickListener{
            mainViewModel.skipToPreviousSong()
        }

        ivSkip.setOnClickListener{
            mainViewModel.skipToNextSong()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setCurPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekBar = true
                }
            }
        })
    }

    private fun updateTitleAndSongImage(song: Song) {
        val title = "${song.title} - ${song.subtitle}"
        tvSongName.text = title
        glide.load(song.imageUrl).into(ivSongImage)
    }

    private fun subscribeToObServers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if (curPlayingSong == null && songs.isNotEmpty()) {
                                curPlayingSong = songs[0]
                                updateTitleAndSongImage(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.curPlayingSong.observe(viewLifecycleOwner) {
            if(it == null) return@observe
            curPlayingSong = it.toSong()
            updateTitleAndSongImage(curPlayingSong!!)
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            ivPlayPauseDetail.setImageResource(
                    if (playbackState?.isPlaying == true)
                        R.drawable.ic_pause
                    else
                        R.drawable.ic_play
            )
            seekBar.progress = it?.position?.toInt() ?: 0
        }

        songViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            if(shouldUpdateSeekBar) {
                seekBar.progress = it.toInt()
                setCurPlayerTimeToTextView(it)
            }
        }

        songViewModel.curSongDuration.observe(viewLifecycleOwner) {
            seekBar.max = it.toInt()
            val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(it) % 60
            val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(it) % 60
            lateinit var tot : String
            tot = if(minutes < 10)
                "0${minutes}"
            else
                "${minutes}"
            tot += if(seconds < 10)
                ":0${seconds}"
            else
                ":${seconds}"
            tvSongDuration.text = tot
        }
    }

    private fun setCurPlayerTimeToTextView(ms: Long) {
        val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        lateinit var tot : String

        tot = if(minutes < 10)
            "0${minutes}"
        else
            "${minutes}"
        tot += if(seconds < 10)
            ":0${seconds}"
        else
            ":${seconds}"

        tvCurTime.text = tot
    }
}