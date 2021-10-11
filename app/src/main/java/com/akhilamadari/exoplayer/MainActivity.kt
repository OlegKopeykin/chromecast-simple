package com.akhilamadari.exoplayer

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.util.Util.getUserAgent
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var castPlayer: CastPlayer
    private var player: SimpleExoPlayer? = null

    val videoStringYO = "https://cdn.dev.yomobile.xyz/test/hls/c35ce4c4-d8a9-49ea-98a3-b98f99d4b8d4/master.m3u8"
    val videoString = videoStringYO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        invalidateOptionsMenu()
        CastContext.getSharedInstance(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.cast, menu)
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.getItemId() == android.R.id.home) {
            Log.e("helooo","pressed")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT <= 23 || player == null)) {
            initPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            player?.release()
            player = null
        }
    }

    private fun hideSystemUi() {
        epvVideo.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun initPlayer() {
        player = ExoPlayerFactory.newSimpleInstance(
            requireNotNull(this),
            DefaultTrackSelector().apply {
                parameters = disableClosedCaptionParams()
            })

        epvVideo.player = player
        castPlayer = CastPlayer(CastContext.getSharedInstance(this))

        castPlayer.setSessionAvailabilityListener(object : CastPlayer.SessionAvailabilityListener {
            override fun onCastSessionAvailable() {
                castPlayer.loadItem(buildMediaQueueItem(videoString),0)
            }
            override fun onCastSessionUnavailable(){
                //  = viewModel.onCastingStateChanged(false, castPlayer.currentPosition)

            }
        })

        val uri = Uri.parse(videoString)
        val httpDataSourceFactory = DefaultHttpDataSourceFactory(getUserAgent(this,"exoplayer"))
        val hlsDataSourceFactory = DefaultHlsDataSourceFactory(httpDataSourceFactory)
        val hlsMediaSourceFactory = HlsMediaSource.Factory(hlsDataSourceFactory)
        val hlsMediaSource = hlsMediaSourceFactory.createMediaSource(uri)


        player?.prepare(hlsMediaSource)
        player?.playWhenReady = true
        player?.seekTo(0)

    }
    private fun buildMediaQueueItem(video :String): MediaQueueItem {
        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        movieMetadata.putString(MediaMetadata.KEY_TITLE, "CBSN News")
        val mediaInfo = MediaInfo.Builder(Uri.parse(video).toString())
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType(MimeTypes.APPLICATION_M3U8)
            .setMetadata(movieMetadata).build()
        return MediaQueueItem.Builder(mediaInfo).build()
    }
    private fun disableClosedCaptionParams() = DefaultTrackSelector.ParametersBuilder()
        .setRendererDisabled(TRACK_TEXT, true)
        .clearSelectionOverrides()
        .build()

    companion object {
        private const val TRACK_TEXT = 2
    }

}