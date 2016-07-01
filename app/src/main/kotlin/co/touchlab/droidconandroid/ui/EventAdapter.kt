package co.touchlab.droidconandroid.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.droidconandroid.R
import co.touchlab.droidconandroid.bindView
import co.touchlab.droidconandroid.data.AppPrefs
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
private const val VIEW_TYPE_NOTIFICATION = 3

private const val HEADER_ITEMS_COUNT = 1

class EventAdapter(private val allEvents: Boolean
                   , initialFilters: List<String>
                   , private val eventClickListener: EventClickListener
                   , var showNotificationCard: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataSet: List<ScheduleBlockHour> = emptyList()
    private var filteredData: ArrayList<ScheduleBlockHour> = ArrayList()
    private var currentTracks: ArrayList<String> = ArrayList(initialFilters)

    override fun getItemCount(): Int {
        return filteredData.size + if (showNotificationCard) HEADER_ITEMS_COUNT else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_EVENT -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
                return ScheduleBlockViewHolder(v)
            }
            VIEW_TYPE_PAST_EVENT, VIEW_TYPE_BLOCK -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_block, parent, false)
                return ScheduleBlockViewHolder(v)
            }
            VIEW_TYPE_NOTIFICATION -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
                return NotificationViewHolder(v)
            }

            else -> throw UnsupportedOperationException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val adjustedPosition = position - if (showNotificationCard) HEADER_ITEMS_COUNT else 0
        if (holder is ScheduleBlockViewHolder) {
            val scheduleBlockHour = filteredData[adjustedPosition]

            ConferenceDataPresenter.styleEventRow(scheduleBlockHour, holder, allEvents)

            if (!scheduleBlockHour.scheduleBlock.isBlock) {
                holder.setOnClickListener { eventClickListener.onEventClick(scheduleBlockHour.scheduleBlock as Event) }
            }
        } else if (holder is NotificationViewHolder) {
            AppPrefs.getInstance(holder.acceptButton.context)
        }
    }

    override fun getItemViewType(position: Int): Int {
        //Position 0 is always the notification
        if (position == 0 && showNotificationCard) return VIEW_TYPE_NOTIFICATION

        val adjustedPosition = position - if (showNotificationCard) HEADER_ITEMS_COUNT else 0

        val item = filteredData[adjustedPosition].scheduleBlock
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

    fun updateNotificationCard(show: Boolean) {
        if(show == showNotificationCard)
            return

        showNotificationCard = show
        if (show)
            notifyItemInserted(0)
        else if(itemCount > 0)
            notifyItemRemoved(0)
    }

    inner abstract class ScheduleCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    inner class ScheduleBlockViewHolder(itemView: View) : ScheduleCardViewHolder(itemView), ConferenceDataPresenter.EventRow {
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
            card.setOnClickListener { listener() }
        }
    }

    inner class NotificationViewHolder(itemView: View) : ScheduleCardViewHolder(itemView) {
        val acceptButton: Button by bindView(R.id.notify_accept)
        val declineButton: Button by bindView(R.id.notify_decline)

        init {
            acceptButton.setOnClickListener {
                EventBusExt.getDefault().post(UpdateAllowNotificationEvent(true))
            }
            declineButton.setOnClickListener {
                EventBusExt.getDefault().post(UpdateAllowNotificationEvent(false))
            }
        }


    }
}

interface EventClickListener {

    fun onEventClick(event: Event)

}

data class UpdateAllowNotificationEvent(val allow: Boolean)

