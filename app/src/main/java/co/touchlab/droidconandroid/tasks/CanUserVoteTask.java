package co.touchlab.droidconandroid.tasks;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.apache.commons.io.IOUtils;

import java.util.ListIterator;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.Task;
import co.touchlab.droidconandroid.BuildConfig;
import co.touchlab.droidconandroid.network.DataHelper;
import co.touchlab.droidconandroid.network.VoteRequest;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

/**
 * Created by toidiu on 7/20/14.
 */
public class CanUserVoteTask extends Task
{
    public  boolean failed   = false;
    public  Boolean canVote  = false;
    private String  authCode = null;

    public CanUserVoteTask()
    {
    }

    public CanUserVoteTask(String authCode)
    {
        this.authCode = authCode;
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        VoteRequest voteRequest = DataHelper.makeRequestAdapter(context).create(VoteRequest.class);
        if(authCode != null && ! authCode.isEmpty())
        {
            Response res = voteRequest.canEBUserVote(BuildConfig.CONVENTION_ID, authCode);

            //parse the body for boolean if user can vote
            canVote = Boolean.parseBoolean(IOUtils.toString(res.getBody().in()));
        }
        else
        {
            canVote = voteRequest.canUserVote(BuildConfig.CONVENTION_ID);
        }
    }

    @Override
    protected final boolean handleError(Context context, Throwable e)
    {
        failed = true;
        if(e instanceof RetrofitError)
        {
            return true;
        }
        else
        {
            Crashlytics.logException(e);
            return false;
        }
    }

    @Override
    protected void onComplete(Context context)
    {
        EventBusExt.getDefault().post(this);
    }


}
