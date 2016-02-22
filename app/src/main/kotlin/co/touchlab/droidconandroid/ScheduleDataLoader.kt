package co.touchlab.droidconandroid

import android.content.Context
import android.text.format.DateUtils
import co.touchlab.android.threading.loaders.AbstractEventBusLoader
import co.touchlab.droidconandroid.data.*
import co.touchlab.droidconandroid.superbus.RefreshScheduleDataKot
import co.touchlab.droidconandroid.tasks.AddRsvpTaskKot
import co.touchlab.droidconandroid.tasks.RemoveRsvpTaskKot
import co.touchlab.squeaky.stmt.Where
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

/**
 * Created by kgalligan on 8/1/14.
 */
class ScheduleDataLoader(val c: Context, val all: Boolean, val day: Long) : AbstractEventBusLoader<List<ScheduleBlock>>(c)
{
    override fun findContent(): List<ScheduleBlock>?
    {
        val databaseHelper = DatabaseHelper.getInstance(getContext())
        val eventDao = databaseHelper.getEventDao()
        val blockDao = databaseHelper.getBlockDao()
        val where = Where<Event>(eventDao)

        val events = if(all)
        {
            where.between("startDateLong", day, day + DateUtils.DAY_IN_MILLIS).query()!!.list()!!
        }
        else
        {
            where.and().between("startDateLong", day, day + DateUtils.DAY_IN_MILLIS).isNotNull("rsvpUuid")!!.query()!!.list()!!
        }

        val blocks = Where<Block>(blockDao).between("startDateLong", day, day + DateUtils.DAY_IN_MILLIS).query().list()

        val eventsAndBlocks = ArrayList<ScheduleBlock>()

        for (event in events) {
            eventDao.fillForeignCollection(event as Event, "speakerList")
        }
        
        eventsAndBlocks.addAll(events as List<Event>)
        eventsAndBlocks.addAll(blocks)

        Collections.sort(eventsAndBlocks, object : Comparator<ScheduleBlock>
        {
            override fun compare(lhs: ScheduleBlock, rhs: ScheduleBlock): Int
            {
                if (lhs.getStartLong() == rhs.getStartLong())
                    return 0

                return lhs.getStartLong()!!.compareTo(rhs.getStartLong()!!)
            }
        })

        return eventsAndBlocks
    }

    override fun handleError(e: Exception?): Boolean
    {
        return false
    }

    public fun onEvent(task: AddRsvpTaskKot)
    {
        onContentChanged()
    }

    public fun onEvent(task: RemoveRsvpTaskKot)
    {
        onContentChanged()
    }

    public fun onEvent(task: RefreshScheduleDataKot)
    {
        onContentChanged()
    }

}