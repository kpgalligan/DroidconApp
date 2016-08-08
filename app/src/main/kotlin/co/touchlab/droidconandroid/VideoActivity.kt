package co.touchlab.droidconandroid

import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import com.google.firebase.crash.FirebaseCrash
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem
import kotlinx.android.synthetic.main.activity_video.*

class VideoActivity : AppCompatActivity(), VideoPlayerEvents.OnFullscreenListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        val link = intent.getStringExtra(EXTRA_STREAM_LINK)
        val cover = intent.getStringExtra(EXTRA_STREAM_COVER)
        if (!TextUtils.isEmpty(link)) {
            val builder = PlaylistItem.Builder().file(link)

            if(!TextUtils.isEmpty(cover))
                builder.image(cover)

            jwplayer.load(builder.build())
        } else {
            FirebaseCrash.report(RuntimeException("Failed to load video link"))
            finish()
        }
    }

    override fun onResume() {
        // Let JW Player know that the app has returned from the background
        super.onResume()
        jwplayer.onResume()
    }

    override fun onPause() {
        // Let JW Player know that the app is going to the background
        jwplayer.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        // Let JW Player know that the app is being destroyed
        jwplayer.onDestroy()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // Set fullscreen when the device is rotated to landscape
        jwplayer.setFullscreen(newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE, true)
        super.onConfigurationChanged(newConfig)
    }

    override fun onFullscreen(state: Boolean) {
        if (state) {
            actionBar.hide()
        } else {
            actionBar.show()
        }
    }
}
