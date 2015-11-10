package co.touchlab.droidconandroid.tasks.persisted;
import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import co.touchlab.android.threading.tasks.helper.RetrofitPersistedTask;
import co.touchlab.droidconandroid.data.AppPrefs;
import co.touchlab.droidconandroid.network.DataHelper;
import co.touchlab.droidconandroid.network.RsvpRequest;
import co.touchlab.droidconandroid.network.VoteRequest;
import retrofit.client.Response;

/**
 * Created by kgalligan on 8/21/15.
 */
public class UpdateVotePersisted extends RetrofitPersistedTask
{
    private Long talkId;
    private int  vote;

    @SuppressWarnings("unused")
    public UpdateVotePersisted()
    {
        setPriority(HIGHER_PRIORITY);
    }

    public UpdateVotePersisted(Long talkId, int vote)
    {
        this.talkId = talkId;
        this.vote = vote;
        setPriority(HIGHER_PRIORITY);
    }

    @Override
    protected void runNetwork(Context context)
    {
        VoteRequest voteRequest = DataHelper.makeRequestAdapter(context).create(VoteRequest.class);

        Response response = voteRequest.updateVote(talkId, vote);
    }

    @Override
    protected boolean handleError(Context context, Throwable e)
    {
        Crashlytics.logException(e);
        return true;
    }
}
