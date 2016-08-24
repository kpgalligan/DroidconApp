package co.touchlab.droidconandroid.tasks

import android.content.Context
import android.util.Log
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.android.threading.tasks.Task
import co.touchlab.droidconandroid.data.SponsorsResponse
import co.touchlab.droidconandroid.presenter.AppManager
import com.google.gson.GsonBuilder
import java.io.InputStreamReader

const val SPONSOR_GENERAL = 0
const val SPONSOR_STREAMING = 1
const val SPONSOR_PARTY = 2

class LoadSponsorsTask(val context: Context, val type: Int) : Task() {
    var response: SponsorsResponse? = null

    override fun run(context: Context?) {
        val inputStream = context?.resources?.assets?.open(getFileName(type))
        response = GsonBuilder().create().fromJson(InputStreamReader(inputStream),
                SponsorsResponse::class.java)
    }

    private fun getFileName(type: Int): String? {
        when (type) {
            SPONSOR_STREAMING -> return "sponsors/sponsors_stream.json"
            SPONSOR_PARTY -> return "sponsors/sponsors_party.json"
            else -> return "sponsors/sponsors_general.json"
        }
    }

    override fun handleError(context: Context?, e: Throwable?): Boolean {
        Log.e("LoadSponsorsTask", "Error loading sponsors", e)
        AppManager.getPlatformClient().logException(e)
        return true
    }

    override fun onComplete(context: Context?) {
        EventBusExt.getDefault()!!.post(this)
    }
}