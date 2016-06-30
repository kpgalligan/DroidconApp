package co.touchlab.droidconandroid

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.droidconandroid.data.Event
import co.touchlab.droidconandroid.data.Track
import co.touchlab.droidconandroid.presenter.ConferenceDataHelper
import co.touchlab.droidconandroid.presenter.ConferenceDayHolder
import co.touchlab.droidconandroid.presenter.ScheduleBlockHour
import co.touchlab.droidconandroid.ui.EventAdapter
import co.touchlab.droidconandroid.ui.EventClickListener
import java.util.*

class ScheduleDataFragment() : Fragment()
{
    var eventList: RecyclerView? = null
    var adapter: EventAdapter? = null
    private var allEvents = true
    private var day: Long? = null
    private var position: Int? = null

    companion object
    {
        val ALL_EVENTS = "ALL_EVENTS"
        val DAY = "DAY"
        val POSITION = "POSITION"

        fun newInstance(all: Boolean, day: Long, position: Int): ScheduleDataFragment
        {
            val scheduleDataFragment = ScheduleDataFragment()
            val args = Bundle()
            args.putBoolean(ALL_EVENTS, all)
            args.putLong(DAY, day)
            args.putInt(POSITION, position)
            scheduleDataFragment.setArguments(args)
            return scheduleDataFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater!!.inflate(R.layout.fragment_schedule_data, null)!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        allEvents = getArguments()!!.getBoolean(ALL_EVENTS)
        day = getArguments()!!.getLong(DAY)
        position = getArguments()!!.getInt(POSITION)

        eventList = view?.findViewById(R.id.eventList) as RecyclerView
        eventList!!.setLayoutManager(LinearLayoutManager(getActivity()))
    }

    override fun onResume()
    {
        super.onResume()
        EventBusExt.getDefault()!!.register(this)
    }

    override fun onPause()
    {
        super.onPause()
        EventBusExt.getDefault()!!.unregister(this)
    }

    fun onEventMainThread(dayHolders: Array<ConferenceDayHolder>?)
    {
        val dayString = ConferenceDataHelper.dateToDayString(Date(day!!))
        for (holder in dayHolders!!) {
            if(holder.dayString!!.equals(dayString))
            {
                updateAdapter(holder.hourHolders)
                break
            }
        }

    }

    private fun updateAdapter(data: Array<out ScheduleBlockHour>?) {
        if (eventList!!.getAdapter() == null)
        {
            adapter = EventAdapter(data!!.asList(), allEvents, (getActivity() as FilterInterface).getCurrentFilters(), object : EventClickListener {
                override fun onEventClick(event: Event) {
                    EventDetailActivity.callMe(getActivity()!!, event.id, event.category)
                }
            })
            eventList!!.setAdapter(adapter!!)
        }
        else
        {
            (eventList!!.getAdapter() as EventAdapter).updateEvents(data!!.asList())
        }
    }

    fun filter(track: Track) {
        if(adapter != null) {
            adapter!!.update(track)
        }
    }

}