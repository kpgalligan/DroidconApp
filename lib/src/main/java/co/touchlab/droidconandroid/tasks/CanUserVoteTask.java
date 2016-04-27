package co.touchlab.droidconandroid.tasks;

import android.content.Context;

import org.apache.commons.io.IOUtils;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.Task;
import co.touchlab.droidconandroid.network.DataHelper;
import co.touchlab.droidconandroid.network.VoteRequest;
import co.touchlab.droidconandroid.presenter.PlatformClient;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by toidiu on 7/20/14.
 */
public class CanUserVoteTask extends Task
{
    public  boolean failed   = false;
    public  Boolean canVote  = false;
    private String  authCode = null;
    private final PlatformClient platformClient;

    public CanUserVoteTask(PlatformClient platformClient)
    {
        this.platformClient = platformClient;
    }

    public CanUserVoteTask(String authCode, PlatformClient platformClient)
    {
        this.authCode = authCode;
        this.platformClient = platformClient;
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        VoteRequest voteRequest = DataHelper.makeRequestAdapter(context, platformClient).create(VoteRequest.class);
        if(authCode != null && ! authCode.isEmpty())
        {
            Response res = voteRequest.canEBUserVote(platformClient.getConventionId(), authCode);

            //parse the body for boolean if user can vote
            canVote = Boolean.parseBoolean(IOUtils.toString(res.getBody().in()));
        }
        else
        {
            canVote = voteRequest.canUserVote(platformClient.getConventionId());
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
            platformClient.logException(e);
            return false;
        }
    }

    @Override
    protected void onComplete(Context context)
    {
        EventBusExt.getDefault().post(this);
    }


}
