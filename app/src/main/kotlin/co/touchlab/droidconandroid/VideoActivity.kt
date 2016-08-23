package co.touchlab.droidconandroid

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.android.threading.tasks.TaskQueue
import co.touchlab.droidconandroid.tasks.CheckWatchVideoTask
import com.google.firebase.crash.FirebaseCrash
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem
import kotlinx.android.synthetic.main.activity_video.*
import java.util.*

class VideoActivity : AppCompatActivity(), VideoPlayerEvents.OnFullscreenListener {
    var handler:Handler? = null
    var checkCount:Int = 0

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
        EventBusExt.getDefault().register(this)
        handler = Handler()
    }

    inner class CheckVideoRunnable : Runnable {
        override fun run() {
            TaskQueue.loadQueueNetwork(this@VideoActivity).execute(CheckWatchVideoTask())
            checkCount++
        }
    }

    @Suppress("unused")
    fun onEventMainThread(task: CheckWatchVideoTask)
    {
        if(task.videoOk)
        {
            checkWatchVideoDelayed()
        }
        else
        {
            Toast.makeText(this, "Another device watching video", Toast.LENGTH_LONG).show();
            finish()
        }
    }

    override fun onResume() {
        // Let JW Player know that the app has returned from the background
        super.onResume()
        jwplayer.onResume()

        checkCount = 0
        checkWatchVideoDelayed()
    }

    private fun checkWatchVideoDelayed() {
        var waitLength: Long = 5 * 60 * 1000
        if(checkCount == 0)
        {
            waitLength = (Random().nextInt(30) + 20) * 1000L
        }
        handler?.postDelayed(CheckVideoRunnable(), waitLength)
    }

    override fun onPause() {
        handler?.removeCallbacks(CheckVideoRunnable())

        // Let JW Player know that the app is going to the background
        jwplayer.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        // Let JW Player know that the app is being destroyed
        jwplayer.onDestroy()
        EventBusExt.getDefault().unregister(this)
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
