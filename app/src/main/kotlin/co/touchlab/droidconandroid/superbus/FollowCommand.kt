package co.touchlab.droidconandroid.superbus

import android.content.Context
import android.util.Log
import co.touchlab.android.threading.tasks.helper.RetrofitPersistedTask
import co.touchlab.android.threading.tasks.persisted.PersistedTask
import co.touchlab.droidconandroid.data.AppPrefs
import co.touchlab.droidconandroid.network.DataHelper
import co.touchlab.droidconandroid.network.FollowRequest
import com.crashlytics.android.Crashlytics

/**
 * Created by kgalligan on 7/20/14.
 */
open class FollowCommand(var otherId: Long? = null) : RetrofitPersistedTask() {
    override fun logSummary(): String {
        return "FollowCommand - " + otherId
    }

    override fun same(command: PersistedTask): Boolean {
        return false
    }

    override fun runNetwork(context: Context?) {
        val restAdapter = DataHelper.makeRequestAdapter(context)
        val followRequest = restAdapter!!.create(javaClass<FollowRequest>())!!

        val userUuid = AppPrefs.getInstance(context).getUserUuid()
        if (userUuid != null) {
            followRequest.follow(otherId!!)
        }
    }

    override fun handleError(context: Context?, e: Throwable?): Boolean {
        Log.w("asdf", "Whoops", e);
        Crashlytics.logException(e);
        return true;
    }
}