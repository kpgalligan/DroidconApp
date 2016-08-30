package co.touchlab.droidconandroid.presenter;
import android.content.Context;
import android.util.Log;

import com.google.j2objc.annotations.Weak;

import co.touchlab.android.threading.tasks.TaskQueue;
import co.touchlab.android.threading.tasks.utils.TaskQueueHelper;
import co.touchlab.droidconandroid.tasks.AddRsvpTask;
import co.touchlab.droidconandroid.tasks.EventDetailLoadTask;
import co.touchlab.droidconandroid.tasks.Queues;
import co.touchlab.droidconandroid.tasks.RemoveRsvpTask;
import co.touchlab.droidconandroid.tasks.StartWatchVideoTask;

/**
 * Created by kgalligan on 4/25/16.
 */
public class EventDetailPresenter extends AbstractEventBusPresenter
{
    private final long                eventId;

    @Weak
    private       EventDetailHost     host;
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

    public void callStartVideo(String link, String cover)
    {
        TaskQueue.loadQueueNetwork(getContext()).execute(new StartWatchVideoTask(link, cover));
    }

    public void onEventMainThread(EventDetailLoadTask task)
    {
        Log.w("asdf", "EventDetailLoadTask "+ eventId);
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

    public void onEventMainThread(StartWatchVideoTask task)
    {
        host.resetStreamProgress();
        if(task.videoOk)
        {
            host.callStreamActivity(task);
        }
        else if(task.unauthorized)
        {
            host.showTicketOptions();
        }
        else
        {
            host.reportError("Couldn't start video. Either server or network issue.");
        }
    }

    public boolean isStreamStarting()
    {
        return TaskQueueHelper.hasTasksOfType(TaskQueue.loadQueueNetwork(getContext()), StartWatchVideoTask.class);
    }

    private boolean ready()
    {
        return eventDetailLoadTask != null;
    }

    public EventDetailLoadTask getEventDetailLoadTask()
    {
        return eventDetailLoadTask;
    }

    @Override
    public void unregister()
    {
        super.unregister();
        host = null;
    }

    public void toggleRsvp()
    {
        if(! ready())
        {
            return;
        }

        if(eventDetailLoadTask.event.isRsvped())
        {
            Queues.localQueue(getContext())
                  .execute(new RemoveRsvpTask(eventDetailLoadTask.event.id));
        }
        else
        {
            Queues.localQueue(getContext()).execute(new AddRsvpTask(eventDetailLoadTask.event.id));
        }
    }
}
