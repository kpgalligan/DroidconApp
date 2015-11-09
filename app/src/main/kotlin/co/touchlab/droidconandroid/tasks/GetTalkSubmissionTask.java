package co.touchlab.droidconandroid.tasks;

import android.content.Context;
import android.util.Log;
import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.Task;
import co.touchlab.droidconandroid.data.DatabaseHelper;
import co.touchlab.droidconandroid.data.Event;
import co.touchlab.droidconandroid.data.TalkSubmission;
import co.touchlab.droidconandroid.network.DataHelper;
import co.touchlab.droidconandroid.network.TalkVotingWrapper;
import co.touchlab.droidconandroid.network.VoteRequest;
import co.touchlab.squeaky.dao.Dao;

import java.util.*;

/**
 * Created by toidiu on 7/20/14.
 */
public class GetTalkSubmissionTask extends Task {
        public  List<Event> list ;

    @Override
    protected void run(Context context) throws Throwable
    {


        VoteRequest voteRequest = DataHelper.makeRequestAdapter(context).create(VoteRequest.class);
        List<TalkVotingWrapper> talkSubmission = voteRequest.getTalkSubmission();

        Log.d("-----------------------ak", talkSubmission.get(0).talkSubmission.description);
        List<TalkSubmission> talkSubmissions = TalkVotingWrapper.parseResp(talkSubmission);


        Dao<TalkSubmission, Long> dao = DatabaseHelper.getInstance(context).getTalkSubDao();
        for(TalkSubmission t: talkSubmissions){
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
            EventBusExt.getDefault().post(this);
    }

}
