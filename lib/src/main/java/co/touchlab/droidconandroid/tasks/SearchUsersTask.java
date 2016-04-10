package co.touchlab.droidconandroid.tasks;
import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;

import co.touchlab.android.threading.tasks.Task;
import co.touchlab.droidconandroid.CrashReport;
import co.touchlab.droidconandroid.PlatformClientContainer;
import co.touchlab.droidconandroid.network.DataHelper;
import co.touchlab.droidconandroid.network.FindUserRequest;
import co.touchlab.droidconandroid.network.dao.UserSearchResponse;
import retrofit.RestAdapter;

/**
 * Created by kgalligan on 4/8/16.
 */
public class SearchUsersTask extends Task
{
    private final String search;
    public UserSearchResponse userSearchResponse;
    private final AtomicBoolean canceled = new AtomicBoolean(false);

    public SearchUsersTask(String search)
    {
        this.search = search;
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        if(canceled.get())
            return;

        RestAdapter restAdapter = DataHelper.makeRequestAdapter(context, PlatformClientContainer.platformClient);
        FindUserRequest findUserRequest = restAdapter.create(FindUserRequest.class);
        userSearchResponse = findUserRequest.searchUsers(search);
    }

    @Override
    protected boolean handleError(Context context, Throwable e)
    {
        CrashReport.logException(e);
        return true;
    }

    @Override
    protected void onComplete(Context context)
    {
        super.onComplete(context);
    }

    public void cancel()
    {
        canceled.set(true);
    }

    public UserSearchResponse getUserSearchResponse()
    {
        return userSearchResponse;
    }
}
