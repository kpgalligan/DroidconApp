package co.touchlab.droidconandroid.presenter;
import android.content.Context;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.Task;

/**
 * Created by kgalligan on 4/17/16.
 */
public class LoadConferenceDataTask extends Task
{
    public ConferenceDayHolder[] conferenceDayHolders;

    @Override
    protected void run(Context context) throws Throwable
    {
        conferenceDayHolders = ConferenceDataHelper.listDays(context);
    }

    @Override
    protected boolean handleError(Context context, Throwable e)
    {
        return false;
    }

    @Override
    protected void onComplete(Context context)
    {
        EventBusExt.getDefault().post(this);
    }
}
