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
    private final boolean openForVote;
    public List<TalkSubmission> list = new ArrayList<>();

    public GetDbTalkSubmissionTask(boolean openForVote)
    {
        this.openForVote = openForVote;
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        Dao<TalkSubmission, Long> dao = DatabaseHelper.getInstance(context).getTalkSubDao();

        if(openForVote)
        {
            list = new Where(dao).isNull("vote").query().list();
        }
        else
        {
            list = new Where(dao).isNotNull("vote").query().list();
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


//
//
//package co.touchlab.droidconandroid.tasks
//
//        import android.content.Context
//        import co.touchlab.android.threading.eventbus.EventBusExt
//        import co.touchlab.android.threading.tasks.Task
//        import co.touchlab.droidconandroid.data.DatabaseHelper
//        import co.touchlab.droidconandroid.data.Event
//        import co.touchlab.squeaky.dao.Dao
//        import co.touchlab.squeaky.stmt.Where
//        import java.util.*
//
///**
// * Created by toidiu on 7/20/14.
// */
//class FindVoteDbTaskKot(val openForVote: Boolean) : Task() {
//        var list: List<Event> = ArrayList()
//
//        override fun run(context: Context?) {
//
//
//
//        val dao: Dao<Event, Long> = DatabaseHelper.getInstance(context).eventDao
//
//        list = when (openForVote) {
//        true -> Where<Event, Long>(dao).isNull("vote")!!.query()!!.list()
//        false -> Where<Event, Long>(dao).isNotNull("vote")!!.query()!!.list()
//        }
//
//        }
//
//        override fun onComplete(context: Context?) {
//        EventBusExt.getDefault() post this
//        }
//
//        override fun handleError(context: Context?, e: Throwable?): Boolean {
//        return true
//        }
//
//        }
