package co.touchlab.droidconandroid.tasks;

import android.content.Context;

import java.util.List;

import co.touchlab.android.threading.tasks.TaskQueue;
import co.touchlab.android.threading.tasks.persisted.PersistedTask;
import co.touchlab.droidconandroid.data.DatabaseHelper;
import co.touchlab.droidconandroid.data.TalkSubmission;
import co.touchlab.droidconandroid.network.DataHelper;
import co.touchlab.droidconandroid.network.TalkVotingWrapper;
import co.touchlab.droidconandroid.network.VoteRequest;
import co.touchlab.droidconandroid.tasks.persisted.PersistedTaskQueueFactory;
import co.touchlab.squeaky.dao.Dao;

/**
 * Created by toidiu on 7/20/14.
 */
public class GetTalkSubmissionTask extends PersistedTask
{
    public List<TalkSubmission> list;


    public GetTalkSubmissionTask()
    {
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        VoteRequest voteRequest = DataHelper.makeRequestAdapter(context).create(VoteRequest.class);
        List<TalkVotingWrapper> talkSubmission = voteRequest.getTalkSubmission();
        list = TalkVotingWrapper.parseResp(talkSubmission);


        Dao<TalkSubmission, Long> dao = DatabaseHelper.getInstance(context).getTalkSubDao();
        for(TalkSubmission t : list)
        {
            dao.update(t);
        }

    }

    @Override
    protected boolean handleError(Context context, Throwable e)
    {
        return false;
    }

    @Override
    protected void onComplete(Context context)
    {
        TaskQueue.loadQueueDefault(context).execute(new GetDbTalkSubmissionTask(true));
    }

}
