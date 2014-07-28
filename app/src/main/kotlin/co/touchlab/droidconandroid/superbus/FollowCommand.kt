package co.touchlab.droidconandroid.superbus

import co.touchlab.android.superbus.CheckedCommand
import retrofit.http.FormUrlEncoded
import retrofit.http.POST
import co.touchlab.android.superbus.errorcontrol.TransientException
import co.touchlab.android.superbus.errorcontrol.PermanentException
import retrofit.http.Path
import retrofit.http.Field
import co.touchlab.droidconandroid.network.BasicIdResult
import co.touchlab.android.superbus.Command
import android.content.Context
import co.touchlab.droidconandroid.network.DataHelper
import co.touchlab.droidconandroid.data.AppPrefs
import android.util.Log
import co.touchlab.droidconandroid.network.AddRsvpRequest
import co.touchlab.droidconandroid.network.RemoveRsvpRequest
import co.touchlab.droidconandroid.network.FollowRequest
import co.touchlab.droidconandroid.network.LoginResult

/**
 * Created by kgalligan on 7/20/14.
 */
open class FollowCommand(var otherId : Long? = null) : CheckedCommand()
{
    override fun logSummary(): String
    {
        return "FollowCommand - " + otherId
    }

    override fun same(command: Command): Boolean
    {
        return false
    }

    override fun callCommand(context: Context)
    {
        val restAdapter = DataHelper.makeRequestAdapter(context)
        val followRequest = restAdapter!!.create(javaClass<FollowRequest>())!!

        val userUuid = AppPrefs.getInstance(context).getUserUuid()
        if(userUuid != null)
        {
            followRequest.follow(userUuid, otherId!!)
        }
    }

    override fun handlePermanentError(context: Context, exception: PermanentException): Boolean
    {
        return false
    }
}