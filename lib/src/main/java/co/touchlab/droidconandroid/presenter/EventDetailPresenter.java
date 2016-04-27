package co.touchlab.droidconandroid.presenter;
import android.content.Context;

import co.touchlab.droidconandroid.tasks.AddRsvpTask;
import co.touchlab.droidconandroid.tasks.EventDetailLoadTask;
import co.touchlab.droidconandroid.tasks.Queues;
import co.touchlab.droidconandroid.tasks.RemoveRsvpTask;

/**
 * Created by kgalligan on 4/25/16.
 */
public class EventDetailPresenter extends AbstractEventBusPresenter
{
    private final long eventId;
    private final EventDetailHost host;
    private       EventDetailLoadTask eventDetailLoadTask;

    public EventDetailPresenter(Context context, long eventId, EventDetailHost host)
    {
        super(context);
        this.eventId = eventId;
        this.host = host;
        refreshData();
    }

    private void refreshData()
    {
        Queues.localQueue(getContext()).execute(new EventDetailLoadTask(this.eventId));
    }

    public void onEventMainThread(EventDetailLoadTask task)
    {
        eventDetailLoadTask = task;
        host.dataRefresh();
    }

    public void onEventMainThread(RemoveRsvpTask task)
    {
        refreshData();
    }

    public void onEventMainThread(AddRsvpTask task)
    {
        refreshData();
    }

    private boolean ready()
    {
        return eventDetailLoadTask != null;
    }

    public EventDetailLoadTask getEventDetailLoadTask()
    {
        return eventDetailLoadTask;
    }

    public void toggleRsvp()
    {
        if(!ready())
            return;

        if(eventDetailLoadTask.event.isRsvped())
        {
            Queues.localQueue(getContext()).execute(new RemoveRsvpTask(eventDetailLoadTask.event.id));
        }
        else
        {
            Queues.localQueue(getContext()).execute(new AddRsvpTask(eventDetailLoadTask.event.id));
        }
    }
}
