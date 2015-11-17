package co.touchlab.droidconandroid.tasks;

import android.content.Context;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.Task;
import co.touchlab.droidconandroid.data.DatabaseHelper;
import co.touchlab.droidconandroid.data.TalkSubmission;
import co.touchlab.droidconandroid.tasks.persisted.PersistedTaskQueueFactory;
import co.touchlab.droidconandroid.tasks.persisted.UpdateVotePersisted;
import co.touchlab.squeaky.dao.Dao;

/**
 * Created by toidiu on 7/20/14.
 */
public class UpdateDbVoteTask extends Task
{
    public final TalkSubmission talk;

    public UpdateDbVoteTask(TalkSubmission talk)
    {
        this.talk = talk;
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        Dao<TalkSubmission, Long> dao = DatabaseHelper.getInstance(context).getTalkSubDao();
        PersistedTaskQueueFactory.getInstance(context).execute(new UpdateVotePersisted(talk.
                                                                                               id,
                                                                                       talk.vote));
        dao.update(talk);
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
