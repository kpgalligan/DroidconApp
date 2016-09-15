package co.touchlab.droidconandroid.presenter;
import android.content.Context;
import android.util.Log;

import com.google.j2objc.annotations.Weak;

import org.jetbrains.annotations.NotNull;

import co.touchlab.android.threading.tasks.TaskQueue;
import co.touchlab.android.threading.tasks.utils.TaskQueueHelper;
import co.touchlab.droidconandroid.data.AppPrefs;
import co.touchlab.droidconandroid.tasks.AddRsvpTask;
import co.touchlab.droidconandroid.tasks.EventDetailLoadTask;
import co.touchlab.droidconandroid.tasks.EventVideoDetailsTask;
import co.touchlab.droidconandroid.tasks.Queues;
import co.touchlab.droidconandroid.tasks.RemoveRsvpTask;
import co.touchlab.droidconandroid.tasks.StartWatchVideoTask;
import co.touchlab.droidconandroid.utils.SlackUtils;

/**
 * Created by kgalligan on 4/25/16.
 */
public class EventDetailPresenter extends AbstractEventBusPresenter
{
    private final long eventId;

    @Weak
    private EventDetailHost       host;
    private EventDetailLoadTask   eventDetailLoadTask;
    private EventVideoDetailsTask eventVideoDetailsTask;

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
        TaskQueue.loadQueueNetwork(getContext())
                .execute(new StartWatchVideoTask(eventId, link, cover));
    }

    public void onEventMainThread(EventDetailLoadTask task)
    {
        Log.w("asdf", "EventDetailLoadTask " + eventId);
        eventDetailLoadTask = task;
        refreshVideoData();
        host.dataRefresh();
    }

    private void refreshVideoData()
    {
        TaskQueue.loadQueueNetwork(getContext()).execute(new EventVideoDetailsTask(eventId));
    }

    public void onEventMainThread(EventVideoDetailsTask task)
    {
        if(task.getEventId() == eventId)
        {
            Log.w("asdf", "EventVideoDetailsTask " + eventId);
            eventVideoDetailsTask = task;
            host.videoDataRefresh();
        }
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
            host.showTicketOptions(AppPrefs.getInstance(getContext()).getEventbriteEmail(),
                    task.link,
                    task.cover);
        }
        else
        {
            host.reportError("Couldn't start video. Either server or network issue.");
        }
    }

    public boolean isStreamStarting()
    {
        return TaskQueueHelper.hasTasksOfType(TaskQueue.loadQueueNetwork(getContext()),
                StartWatchVideoTask.class);
    }

    private boolean ready()
    {
        return eventDetailLoadTask != null;
    }

    public EventDetailLoadTask getEventDetailLoadTask()
    {
        return eventDetailLoadTask;
    }

    public EventVideoDetailsTask getEventVideoDetailsTask()
    {
        return eventVideoDetailsTask;
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

    public void setEventbriteEmail(String email, String link, String cover)
    {
        AppPrefs.getInstance(getContext()).setEventbriteEmail(email);
        callStartVideo(link, cover);
    }

    public void openSlack()
    {
        String slackLink = SlackUtils.createSlackLink(eventDetailLoadTask.event.venue);
        String slackLinkHttp = SlackUtils.createSlackLinkHttp(eventDetailLoadTask.event.venue);
        host.openSlack(slackLink,
                slackLinkHttp,
                AppPrefs.getInstance(getContext()).getShowSlackDialog());
    }
}
