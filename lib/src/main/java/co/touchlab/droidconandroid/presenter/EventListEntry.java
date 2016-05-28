package co.touchlab.droidconandroid.presenter;
import android.text.TextUtils;

import co.touchlab.droidconandroid.data.Event;
import co.touchlab.droidconandroid.data.ScheduleBlock;

/**
 * Created by kgalligan on 4/22/16.
 */
public class EventListEntry
{
    public enum ItemViewType
    {
        PastEvent, Event, Block
    }

    public final ScheduleBlock scheduleBlock;
    public final String        timeDisplay;
    public final boolean       isConflict;

    public EventListEntry(ScheduleBlock event, String timeDisplay, boolean isConflict)
    {
        this.scheduleBlock = event;
        this.timeDisplay = timeDisplay;
        this.isConflict = isConflict;
    }

    public boolean isRsvped()
    {
        String rsvpUuid = null;

        if(scheduleBlock instanceof Event)
            rsvpUuid = ((Event)scheduleBlock).rsvpUuid;

        return ! TextUtils.isEmpty(rsvpUuid);
    }

    public ItemViewType getItemViewType()
    {
        if(scheduleBlock instanceof Event)
        {
            final Event event = (Event) this.scheduleBlock;
            return event.isPast() ? ItemViewType.PastEvent : ItemViewType.Event;
        }
        else
        {
            return ItemViewType.Block;
        }
    }
}
