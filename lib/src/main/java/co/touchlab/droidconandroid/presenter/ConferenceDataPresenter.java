package co.touchlab.droidconandroid.presenter;
import android.content.Context;
import android.util.Log;

import com.google.j2objc.annotations.AutoreleasePool;

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
}
