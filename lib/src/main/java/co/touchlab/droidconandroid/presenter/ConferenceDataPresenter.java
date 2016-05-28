package co.touchlab.droidconandroid.presenter;
import android.content.Context;
import android.util.Log;

import com.google.j2objc.annotations.AutoreleasePool;
import com.google.j2objc.annotations.Weak;

import co.touchlab.droidconandroid.data.Block;
import co.touchlab.droidconandroid.data.Event;
import co.touchlab.droidconandroid.tasks.Queues;
import co.touchlab.droidconandroid.tasks.SeedScheduleDataTask;
import co.touchlab.droidconandroid.tasks.persisted.RefreshScheduleData;

/**
 * Created by kgalligan on 4/15/16.
 */
public class ConferenceDataPresenter extends AbstractEventBusPresenter
{
    private final ConferenceDataHost conferenceDataHost;
    private final boolean allEvents;

    public ConferenceDataPresenter(Context context, ConferenceDataHost conferenceDataHost, boolean allEvents)
    {
        super(context);
        this.conferenceDataHost = conferenceDataHost;
        this.allEvents = allEvents;
        refreshConferenceData();
    }

    public void onEventMainThread(SeedScheduleDataTask task)
    {
        refreshConferenceData();
    }

    @AutoreleasePool
    public void refreshConferenceData()
    {
        Queues.localQueue(getContext()).execute(new LoadConferenceDataTask(allEvents));
    }

    public void onEventMainThread(LoadConferenceDataTask task)
    {
        Log.w(ConferenceDataPresenter.class.getSimpleName(), "LoadConferenceDataTask returned");
        conferenceDataHost.loadCallback(task.conferenceDayHolders);
    }

    public void onEventMainThread(RefreshScheduleData task)
    {
        refreshConferenceData();
    }

    public static void styleEventRow(ScheduleBlockHour scheduleBlockHour, EventRow row, boolean allEvents)
    {
//        val context = holder!!.itemView.getContext()
//        val resources = context.getResources()
//        holder as ScheduleBlockViewHolder
//        val scheduleBlockHour = filteredData.get(position)

        boolean typ = scheduleBlockHour.getScheduleBlock() instanceof Event;

        Log.w("asdf", "qwert");
        if(scheduleBlockHour.getScheduleBlock().isBlock())
        {
            Block block = (Block)scheduleBlockHour.scheduleBlock;

            row.setTitleText(block.name);
            row.setTimeText(scheduleBlockHour.hourStringDisplay);
            row.setDetailText("");
            row.setRsvpVisible(false);

//            holder.title.setText(block.name)
//            holder.time.setText(scheduleBlockHour.hourStringDisplay)
//            holder.locationTime.setText(getDetailedTime(block))
//            holder.rsvp.setVisibility(View.GONE)
        }
        else
        {
            Event event = (Event)scheduleBlockHour.scheduleBlock;

            row.setTitleText(event.name);

            row.setTimeText(scheduleBlockHour.hourStringDisplay);

//            holder.card.setOnClickListener{
//            eventClickListener.onEventClick(event)
//        }

            if(event.isRsvped())
            {
                row.setRsvpVisible(true);
                if(event.isNow())
                    row.setRsvpChecked();
                //TODO: Check conflict
//                else if(!event.isPast() && EventDetailLoadTask.hasConflict(event, dataSet))
//                    holder.rsvp.setImageDrawable(ResourcesCompat.getDrawable(context, R.drawable.ic_check_red))
                else if(allEvents)
                    row.setRsvpChecked();
                else
                    row.setRsvpVisible(false);
            } else {
                row.setRsvpVisible(false);
            }
//            if (!TextUtils.isEmpty(event.rsvpUuid)) {
//                holder.rsvp.setVisibility(View.VISIBLE)
//                if(event.isNow())
//                    holder.rsvp.setImageDrawable(ResourcesCompat.getDrawable(context, R.drawable.ic_play))
//                else if(!event.isPast() && EventDetailLoadTask.hasConflict(event, dataSet))
//                    holder.rsvp.setImageDrawable(ResourcesCompat.getDrawable(context, R.drawable.ic_check_red))
//                else if(allEvents)
//                    holder.rsvp.setImageDrawable(ResourcesCompat.getDrawable(context, R.drawable.ic_check_green))
//                else
//                    row.setRsvpVisible(false);
//            } else {
//                row.setRsvpVisible(false);
//            }

            row.setDetailText(event.allSpeakersString());

//            val track = Track.findByServerName(event.category)
//            if(track != null && !event.isPast()) {
//
//                holder.track.setBackgroundColor(resources.getColor(context.resources.getIdentifier(track.getTextColorRes(), "color", context.packageName)))
//            }
//            else
//            {
//                holder.track.setBackgroundColor(resources.getColor(android.R.color.transparent))
//            }
        }
        /*if(getItemViewType(position) == VIEW_TYPE_EVENT || getItemViewType(position) == VIEW_TYPE_PAST_EVENT){

            val event = scheduleBlockHour.scheduleBlock as Event

            holder.title.setText(event.name)

            holder.time.setText(scheduleBlockHour.hourStringDisplay)

            holder.card.setOnClickListener{
                eventClickListener.onEventClick(event)
            }

            if (!TextUtils.isEmpty(event.rsvpUuid)) {
                holder.rsvp.setVisibility(View.VISIBLE)
                if(event.isNow())
                    holder.rsvp.setImageDrawable(ResourcesCompat.getDrawable(context, R.drawable.ic_play))
                else if(!event.isPast() && EventDetailLoadTask.hasConflict(event, dataSet))
                    holder.rsvp.setImageDrawable(ResourcesCompat.getDrawable(context, R.drawable.ic_check_red))
                else if(allEvents)
                    holder.rsvp.setImageDrawable(ResourcesCompat.getDrawable(context, R.drawable.ic_check_green))
                else
                    holder.rsvp.setVisibility(View.GONE)
            } else {
                holder.rsvp.setVisibility(View.GONE)
            }

            holder.locationTime.setText("${event.allSpeakersString()}")

            val track = Track.findByServerName(event.category)
            if(track != null && !event.isPast()) {

                holder.track.setBackgroundColor(resources.getColor(context.resources.getIdentifier(track.getTextColorRes(), "color", context.packageName)))
            }
            else
            {
                holder.track.setBackgroundColor(resources.getColor(android.R.color.transparent))
            }
        } else if (getItemViewType(position) == VIEW_TYPE_BLOCK) {
            val block = scheduleBlockHour.scheduleBlock as Block

            holder.title.setText(block.name)
            holder.time.setText(scheduleBlockHour.hourStringDisplay)
            holder.locationTime.setText(getDetailedTime(block))
            holder.rsvp.setVisibility(View.GONE)

        }*/
    }

    public interface EventRow
    {
        void setTitleText(String s);
        void setTimeText(String s);
        void setDetailText(String s);
        void setRsvpVisible(boolean b);
        void setRsvpChecked();
        void setRsvpConflict();

    }
}
