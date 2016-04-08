package co.touchlab.droidconandroid.tasks.persisted;
import android.content.Context;

import co.touchlab.droidconandroid.PlatformClientContainer;
import co.touchlab.droidconandroid.network.DataHelper;
import co.touchlab.droidconandroid.network.VoteRequest;

/**
 * Created by kgalligan on 8/21/15.
 */
public class UpdateVotePersisted extends VotePersistedTask
{
    private Long talkId;
    private int  vote;

    public static void startMe(Context context, Long talkId, int vote)
    {
        getQueue(context).execute(new UpdateVotePersisted(talkId, vote));
    }

    @SuppressWarnings("unused")
    public UpdateVotePersisted()
    {
        setPriority(HIGHER_PRIORITY);
    }

    public UpdateVotePersisted(Long talkId, int vote)
    {
        this();
        this.talkId = talkId;
        this.vote = vote;
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        VoteRequest voteRequest = DataHelper.makeRequestAdapter(context, PlatformClientContainer.platformClient).create(VoteRequest.class);
        voteRequest.updateVote(PlatformClientContainer.platformClient.getConventionId(), talkId, vote);

    }

    @Override
    protected String errorString()
    {
        return "Something went wrong. Failed to register your vote.";
    }

}
