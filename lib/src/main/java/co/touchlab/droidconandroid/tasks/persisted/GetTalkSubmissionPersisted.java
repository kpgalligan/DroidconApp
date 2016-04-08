package co.touchlab.droidconandroid.tasks.persisted;

import android.content.Context;

import java.sql.SQLException;
import java.util.List;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.persisted.PersistedTask;
import co.touchlab.droidconandroid.PlatformClientContainer;
import co.touchlab.droidconandroid.data.DatabaseHelper;
import co.touchlab.droidconandroid.data.TalkSubmission;
import co.touchlab.droidconandroid.network.DataHelper;
import co.touchlab.droidconandroid.network.TalkVotingWrapper;
import co.touchlab.droidconandroid.network.VoteRequest;
import co.touchlab.squeaky.dao.Dao;

/**
 * Created by toidiu on 7/20/14.
 */
public class GetTalkSubmissionPersisted extends VotePersistedTask
{
    public List<TalkSubmission> list;

    public static void startMe(Context context)
    {
        //should CALL ONLY FROM VoteFragment
        getQueue(context).execute(new GetTalkSubmissionPersisted());
    }

    public GetTalkSubmissionPersisted()
    {
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        VoteRequest voteRequest = DataHelper.makeRequestAdapter(context, PlatformClientContainer.platformClient).create(VoteRequest.class);
        List<TalkVotingWrapper> talkSubmission = voteRequest.getTalkSubmission(
                PlatformClientContainer.platformClient.getConventionId());
        list = TalkVotingWrapper.parseResp(talkSubmission);


        final Dao<TalkSubmission> dao = DatabaseHelper.getInstance(context).getTalkSubDao();
        DatabaseHelper.getInstance(context).inTransaction(new Runnable()
        {
            @Override
            public void run()
            {
                for(TalkSubmission t : list)
                {
                    try
                    {
                        assignRandomInt(t, dao);
                        dao.createOrUpdate(t);

                    }
                    catch(SQLException e)
                    {
                        throw new RuntimeException(e);
                    }

                }
            }
        });
    }

    private void assignRandomInt(TalkSubmission t, Dao<TalkSubmission> dao) throws SQLException
    {
        TalkSubmission dbTalk = dao.queryForId(t.id);
        if(dbTalk != null)
        {
            t.random = dbTalk.random;
        }
        else
        {
            while(true)
            {
                Integer randInt = TalkSubmission.getRandInt();
                List<TalkSubmission> randList = dao.queryForEq("random", randInt).list();
                if(randList.isEmpty())
                {
                    t.random = randInt;
                    break;
                }
            }
        }
    }

    @Override
    protected boolean same(PersistedTask persistedTask)
    {
        return persistedTask instanceof GetTalkSubmissionPersisted;
    }

    @Override
    protected String errorString()
    {
        return "Something went wrong. Failed to get new talks.";
    }

    @Override
    protected void onComplete(Context context)
    {
        EventBusExt.getDefault().post(this);
    }

}
