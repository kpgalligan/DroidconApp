package co.touchlab.droidconandroid.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import co.touchlab.droidconandroid.R
import co.touchlab.droidconandroid.bindView
import co.touchlab.droidconandroid.data.Block
import co.touchlab.droidconandroid.data.Event
import co.touchlab.droidconandroid.data.Track
import co.touchlab.droidconandroid.presenter.ConferenceDataPresenter
import co.touchlab.droidconandroid.presenter.ScheduleBlockHour
import com.wnafee.vector.compat.ResourcesCompat
import java.util.*

/**
 *
 * Created by izzyoji :) on 8/6/15.
 */

private const val VIEW_TYPE_EVENT = 0
private const val VIEW_TYPE_BLOCK = 1
private const val VIEW_TYPE_PAST_EVENT = 2

class EventAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private var dataSet: List<ScheduleBlockHour> = emptyList()
    private var filteredData: ArrayList<ScheduleBlockHour> = ArrayList()
    private val eventClickListener: EventClickListener
    private val allEvents: Boolean
    private var currentTracks: ArrayList<String> = ArrayList()

    constructor( all: Boolean, initialFilters: List<String>, eventClickListener: EventClickListener) : super() {
        allEvents = all
        this.eventClickListener = eventClickListener
        currentTracks = ArrayList(initialFilters)
        updateData()
    }

    override fun getItemCount(): Int {
        return filteredData.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = when (viewType)
        {
            VIEW_TYPE_EVENT -> LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
            VIEW_TYPE_PAST_EVENT, VIEW_TYPE_BLOCK -> LayoutInflater.from(parent.context).inflate(R.layout.item_block, parent, false)
            else -> throw UnsupportedOperationException()
        }
        return ScheduleBlockViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ScheduleBlockViewHolder
        val scheduleBlockHour = filteredData[position]

        ConferenceDataPresenter.styleEventRow(scheduleBlockHour, holder, allEvents)

        if (!scheduleBlockHour.scheduleBlock.isBlock) {
            holder.setOnClickListener { eventClickListener.onEventClick(scheduleBlockHour.scheduleBlock as Event) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = filteredData[position].scheduleBlock
        when (item) {
            is Event -> return if (item.isPast) VIEW_TYPE_PAST_EVENT else VIEW_TYPE_EVENT
            is Block -> return VIEW_TYPE_BLOCK
            else -> throw UnsupportedOperationException()
        }
    }

    fun toggleTrackFilter(track: Track) {
        val trackServerName = track.serverName
        if (!currentTracks.contains(trackServerName)) {
            currentTracks.add(trackServerName)
        } else {
            currentTracks.remove(trackServerName)
        }
        updateData()
    }

    private fun updateData() {
        filteredData.clear()
        if (currentTracks.isEmpty()) {
            filteredData = ArrayList(dataSet)
        } else {
            //TODO: Filter
            /*for (item in dataSet) {
                if(item is Block) {
                    filteredData.add(item)
                } else {
                    val event = item as Event
                    val category = event.category
                    if (!TextUtils.isEmpty(category) && currentTracks.contains(category)) {
                        filteredData.add(item)
                    }
                }
            }*/
        }

        notifyDataSetChanged()
    }

    fun updateEvents(data: List<ScheduleBlockHour>) {
        dataSet = data
        updateData()
    }

    inner class ScheduleBlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ConferenceDataPresenter.EventRow {
        private val title: TextView by bindView(R.id.title)
        private val time: TextView by bindView(R.id.time)
        private val locationTime: TextView by bindView(R.id.location_time)
        private val card: View by bindView(R.id.card)
        private val rsvp: ImageView by bindView(R.id.rsvp)

        override fun setTitleText(s: String?) {
            title.text = s
        }

        override fun setTimeText(s: String?) {
            time.text = s
        }

        override fun setDetailText(s: String?) {
            locationTime.text = s
        }

        override fun setRsvpVisible(b: Boolean) {
            rsvp.visibility = if (b) View.VISIBLE else View.GONE
        }

        override fun setRsvpChecked() {
            rsvp.setImageDrawable(ResourcesCompat.getDrawable(itemView.context, R.drawable.ic_check_green))
        }

        override fun setRsvpConflict() {
            rsvp.setImageDrawable(ResourcesCompat.getDrawable(itemView.context, R.drawable.ic_check_red))
        }

        fun setOnClickListener(listener: () -> Unit) {
            card.setOnClickListener({ listener() })
        }
    }
}

interface EventClickListener {

    fun onEventClick(event: Event)

}
