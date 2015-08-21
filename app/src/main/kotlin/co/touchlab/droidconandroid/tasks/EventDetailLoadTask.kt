package co.touchlab.droidconandroid.tasks

import android.content.Context
import co.touchlab.droidconandroid.data.Event
import java.util.ArrayList
import co.touchlab.droidconandroid.data.UserAccount
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.droidconandroid.ui.EventAdapter
import co.touchlab.droidconandroid.ui.hasConflict

/**
 * Created by kgalligan on 7/20/14.
 */
open class EventDetailLoadTask(c: Context, val eventId: Long) : DatabaseTaskKot(c)
{
    override fun handleError(context: Context?, e: Throwable?): Boolean {
        return false
    }

    var event: Event? = null
    var conflict = false
    var speakers: ArrayList<UserAccount>? = null

    override fun run(context: Context?)
    {
        val dao = databaseHelper.getEventDao()
        event = dao.queryForId(eventId)

        if(event!!.isRsvped())
            conflict = hasConflict(event!!, dao.queryForAll())

        val eventSpeakerDao = databaseHelper.getEventSpeakerDao()
        val results = eventSpeakerDao.queryBuilder()!!.where()!!.eq("event_id", eventId)!!.query()!!

        speakers = ArrayList(results.size)

        for (eventSpeaker in results)
        {
            speakers!!.add(eventSpeaker.userAccount!!)
        }

        EventBusExt.getDefault()?.post(this)
    }
}