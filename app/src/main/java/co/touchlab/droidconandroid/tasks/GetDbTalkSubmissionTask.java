package co.touchlab.droidconandroid.tasks;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.Task;
import co.touchlab.droidconandroid.data.DatabaseHelper;
import co.touchlab.droidconandroid.data.TalkSubmission;
import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.stmt.Where;

/**
 * Created by toidiu on 7/20/14.
 */
public class GetDbTalkSubmissionTask extends Task
{
    public final boolean openVotes;
    public List<TalkSubmission> list = new ArrayList<>();

    public GetDbTalkSubmissionTask(boolean openForVote)
    {
        this.openVotes = openForVote;
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        Dao<TalkSubmission, Long> dao = DatabaseHelper.getInstance(context).getTalkSubDao();

        if(openVotes)
        {
            list = new Where<TalkSubmission, Long>(dao).isNull("vote").query().orderBy("random").list();
        }
        else
        {
            list = new Where<TalkSubmission, Long>(dao).isNotNull("vote").query().orderBy("random").list();
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
        EventBusExt.getDefault().post(this);
    }

}
