package co.touchlab.droidconandroid.ui

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import co.touchlab.droidconandroid.R
import co.touchlab.droidconandroid.data.Block
import co.touchlab.droidconandroid.data.Event
import co.touchlab.droidconandroid.data.ScheduleBlock
import co.touchlab.droidconandroid.data.Track
import co.touchlab.droidconandroid.network.dao.Convention
import com.wnafee.vector.compat.ResourcesCompat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

//fun hasConflict(event: Event, dataSet: List<ScheduleBlock>):Boolean
//{
//    for (ce in dataSet) {
//        if(ce is Event) {
//            if (event.id != ce.id && !TextUtils.isEmpty(ce.rsvpUuid) && event.startDateLong < ce.endDateLong && event.endDateLong > ce.startDateLong)
//                return true
//        }
//    }
//
//    return false
//}
/**
 *
 * Created by izzyoji :) on 8/6/15.
 */
class VoteAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    val VIEW_TYPE_VOTE: Int = 0

    private var dataSet: List<Event>
//    private var filteredData: ArrayList<ScheduleBlock>
    private val voteClickListener: VoteClickListener

    constructor(events: List<Event>, initialFilters: List<String>,  eventClickListener: VoteClickListener) : super() {
        dataSet = events;
//        filteredData = ArrayList(events);
        this.voteClickListener = eventClickListener
//        this.currentTracks = ArrayList(initialFilters);
//        update(null)
    }

    override fun getItemCount(): Int {
        return dataSet.size()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        val v: View
        if (viewType == VIEW_TYPE_VOTE) {
            v = LayoutInflater.from(parent!!.getContext()).inflate(R.layout.item_vote, parent, false)
            return VoteBlockViewHolder(v)
        }
        throw UnsupportedOperationException()
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val context = holder!!.itemView.getContext()
        val resources = context.getResources()
        holder as VoteBlockViewHolder
        if(getItemViewType(position) == VIEW_TYPE_VOTE ){

            val event = dataSet.get(position) as Event

            holder.title.setText(event.name)
            holder.descrip.setText(event.description)

//            holder.time.setText(getTimeBlock(event, position))

            holder.card.setOnClickListener{
                voteClickListener.onEventClick(event)
            }

//            if (!TextUtils.isEmpty(event.rsvpUuid)) {
//                holder.rsvp.setVisibility(View.VISIBLE)
//                if(event.isNow())
//                    holder.rsvp.setImageDrawable(ResourcesCompat.getDrawable(context, R.drawable.ic_play))
//                else if(!event.isPast() && hasConflict(event, dataSet))
//                    holder.rsvp.setImageDrawable(ResourcesCompat.getDrawable(context, R.drawable.ic_check_red))
//                else
//                    holder.rsvp.setVisibility(View.GONE)
//            } else {
//                holder.rsvp.setVisibility(View.GONE)
//            }
//
//            holder.locationTime.setText("${event.allSpeakersString()}")
//
//            val track = Track.findByServerName(event.category)
//            if(track != null && !event.isPast()) {
//                holder.track.setBackgroundColor(resources.getColor(track.getTextColorRes()))
//            }
//            else
//            {
//                holder.track.setBackgroundColor(resources.getColor(android.R.color.transparent))
//            }

        }
    }

//    private fun getTimeBlock(scheduleBlock: ScheduleBlock, position: Int): String {
//        var timeBlock = ""
//        if (scheduleBlock.getStartLong() != null && isFirstForTime(position)) {
//            val startDate = Date(scheduleBlock.getStartLong())
//            timeBlock = timeFormat.format(startDate).toLowerCase()
//        }
//        return timeBlock
//    }

//    private fun getDetailedTime(scheduleBlock: ScheduleBlock): String? {
//        var time = ""
//
//        if (scheduleBlock.getStartLong() != null && scheduleBlock.getEndLong() != null) {
//            val startDate = Date(scheduleBlock.getStartLong()!!)
//            val endDate = Date(scheduleBlock.getEndLong()!!)
//
//            var formattedStart = timeFormat.format(startDate).toLowerCase()
//            val formattedEnd = timeFormat.format(endDate).toLowerCase()
//
//            val startMarker = formattedStart.substring(Math.max(formattedStart.length() - 2, 0))
//            val endMarker = formattedEnd.substring(Math.max(formattedEnd.length() - 2, 0))
//
//            if (TextUtils.equals(startMarker, endMarker)) {
//                formattedStart = formattedStart.substring(0, Math.max(formattedStart.length() - 2, 0))
//            }
//
//            time = formattedStart + " - " + formattedEnd
//        }
//        return time
//    }

//    private fun isFirstForTime(position: Int): Boolean {
//        if (position == 0) {
//            return true
//        } else {
//            val prevEvent = filteredData.get(position - 1)
//            val event = filteredData.get(position)
//            val prevEventStart = prevEvent.getStartLong()
//            if(prevEventStart != null && (event.getStartLong() != prevEventStart)) {
//                return true
//            }
//        }
//        return false
//    }

    override fun getItemViewType(position: Int): Int {
        val item = dataSet.get(position)
        if (item is Event) {
            return VIEW_TYPE_VOTE
        }
        throw UnsupportedOperationException()
    }

//    fun update(track: Track?) {
//        if(track != null)
//        {
//            val trackServerName = track.getServerName()
//            if(!currentTracks.contains(trackServerName))
//            {
//                currentTracks.add(trackServerName)
//            }
//            else
//            {
//                currentTracks.remove(trackServerName)
//            }
//        }
//
//        filteredData.clear()
//        if(currentTracks.isEmpty())
//        {
//            filteredData = ArrayList(dataSet)
//        } else {
//            for (item in dataSet) {
//                if(item is Block) {
//                    filteredData.add(item)
//                } else {
//                    val event = item as Event
//                    val category = event.category
//                    if (!TextUtils.isEmpty(category) && currentTracks.contains(category)) {
//                        filteredData.add(item)
//                    }
//                }
//            }
//        }
//
//        notifyDataSetChanged()
//    }

//    fun updateEvents(data: List<ScheduleBlock>)
//    {
//        dataSet = data
//        filteredData = ArrayList(data)
//        update(null)
//    }

    public class VoteBlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        public val title: TextView
        public val descrip: TextView
//        public val locationTime: TextView
//        public val track: View
        public val card: View
//        public val rsvp: ImageView

        init {
            title = itemView.findViewById(R.id.title) as TextView
            descrip = itemView.findViewById(R.id.description) as TextView
//            locationTime = itemView.findViewById(R.id.location_time) as TextView
//            track = itemView.findViewById(R.id.track)
            card = itemView.findViewById(R.id.card)
//            rsvp = itemView.findViewById(R.id.rsvp) as ImageView
        }
    }

}

interface VoteClickListener {

    fun onEventClick(event: Event)

}
