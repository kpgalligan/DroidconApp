package co.touchlab.droidconandroid.tasks

import android.content.Context
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.android.threading.tasks.Task
import co.touchlab.droidconandroid.data.DatabaseHelper
import co.touchlab.droidconandroid.data.Event
import co.touchlab.droidconandroid.data.EventSpeaker
import co.touchlab.droidconandroid.data.UserAccount
import co.touchlab.droidconandroid.ui.hasConflict
import co.touchlab.squeaky.stmt.Where
import java.util.ArrayList

/**
 * Created by kgalligan on 7/20/14.
 */
open class EventDetailLoadTask(val eventId: Long) : Task()
{
    override fun handleError(context: Context?, e: Throwable?): Boolean {
        return false
    }

    var event: Event? = null
    var conflict = false
    var speakers: ArrayList<UserAccount>? = null

    override fun run(context: Context?)
    {
        val dao = DatabaseHelper.getInstance(context).eventDao
        event = dao.queryForId(eventId)

        if(event!!.isRsvped())
            conflict = hasConflict(event!!, dao.queryForAll().list())

        val eventSpeakerDao = DatabaseHelper.getInstance(context).getEventSpeakerDao()
//        val results = Where<EventSpeaker, Long>(eventSpeakerDao).eq("event_id", eventId)!!.query()!!.list()!!

        val results = eventSpeakerDao.queryForEq("event_id", eventId).list()

        speakers = ArrayList(results.size())

        for (eventSpeaker in results)
        {
            speakers!!.add(eventSpeaker.userAccount!!)
        }

        event!!.speakerList = results
    }

    override fun onComplete(context: Context?) {
        EventBusExt.getDefault()?.post(this)
    }
}