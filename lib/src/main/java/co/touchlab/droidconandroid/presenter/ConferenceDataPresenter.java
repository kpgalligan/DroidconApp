package co.touchlab.droidconandroid.presenter;
import android.content.Context;
import android.util.Log;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.droidconandroid.tasks.Queues;
import co.touchlab.droidconandroid.tasks.SeedScheduleDataTask;

/**
 * Created by kgalligan on 4/15/16.
 */
public class ConferenceDataPresenter
{
    private final Context context;
    private final ConferenceDataHost conferenceDataHost;

    public ConferenceDataPresenter(Context context, ConferenceDataHost conferenceDataHost)
    {
        this.context = context;
        this.conferenceDataHost = conferenceDataHost;
        EventBusExt.getDefault().register(this);
    }

    public void unregister()
    {
        EventBusExt.getDefault().unregister(this);
    }

    public void seedData(String seed)
    {
        Queues.localQueue(context).execute(new SeedScheduleDataTask(seed));
    }

    public void onEventMainThread(SeedScheduleDataTask task)
    {
        refreshConferenceData();
    }

    public void refreshConferenceData()
    {
        Queues.localQueue(context).execute(new LoadConferenceDataTask());
    }

    public void onEventMainThread(LoadConferenceDataTask task)
    {
        Log.w(ConferenceDataPresenter.class.getSimpleName(), "LoadConferenceDataTask returned");
        conferenceDataHost.loadCallback(task.conferenceDayHolders);
    }

    public void loginUser(String token, String name)
    {
        Queues.localQueue(context).execute(new IosGoogleLoginTask(token, name));
    }

    public void onEventMainThread(IosGoogleLoginTask task)
    {
        Log.w(ConferenceDataPresenter.class.getSimpleName(), "What?!");
    }
}
