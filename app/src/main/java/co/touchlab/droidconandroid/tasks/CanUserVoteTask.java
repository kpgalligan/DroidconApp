package co.touchlab.droidconandroid.tasks;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.Task;
import co.touchlab.droidconandroid.network.DataHelper;
import co.touchlab.droidconandroid.network.VoteRequest;
import co.touchlab.droidconandroid.utils.Toaster;
import retrofit.RetrofitError;

/**
 * Created by toidiu on 7/20/14.
 */
public class CanUserVoteTask extends Task
{
    public Boolean canVote;

    @Override
    protected void run(Context context) throws Throwable
    {
        VoteRequest voteRequest = DataHelper.makeRequestAdapter(context).create(VoteRequest.class);
        canVote = voteRequest.canUserVote();
        Log.d("ak-------", String.valueOf(canVote));

    }

    @Override
    protected final boolean handleError(Context context, Throwable e)
    {
        if(e instanceof RetrofitError)
        {
            return true;
        }
        else
        {
            Crashlytics.logException(e);
            return true;
        }
    }

    @Override
    protected void onComplete(Context context)
    {
        EventBusExt.getDefault().post(this);
    }


}
