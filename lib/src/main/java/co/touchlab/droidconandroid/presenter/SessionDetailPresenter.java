package co.touchlab.droidconandroid.presenter;
import android.content.Context;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.Task;
import co.touchlab.droidconandroid.data.Event;
import co.touchlab.droidconandroid.tasks.AddRsvpTask;
import co.touchlab.droidconandroid.tasks.Queues;
import co.touchlab.droidconandroid.tasks.RemoveRsvpTask;

/**
 * Created by kgalligan on 4/23/16.
 */
public class SessionDetailPresenter
{
    private final Context context;

    public SessionDetailPresenter(Context context)
    {
        this.context = context;
        EventBusExt.getDefault().register(this);
    }

    public void rsvpEvent(Event event)
    {
        Task task = null;
        if(event.isRsvped())
            task = new RemoveRsvpTask(event.id);
        else
            task = new AddRsvpTask(event.id);

        Queues.localQueue(context).execute(task);
    }

    public void onEventMainThread(AddRsvpTask task)
    {

    }

    public void unregister()
    {
        EventBusExt.getDefault().unregister(this);
    }
}
