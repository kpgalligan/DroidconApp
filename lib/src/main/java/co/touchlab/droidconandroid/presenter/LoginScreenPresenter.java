package co.touchlab.droidconandroid.presenter;
import android.content.Context;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.droidconandroid.tasks.Queues;
import co.touchlab.droidconandroid.tasks.UpdatedGoogleLoginTask;

/**
 * Created by kgalligan on 4/19/16.
 */
public class LoginScreenPresenter
{
    private final Context context;
    private final Host host;

    public interface Host
    {
        void onLoginReturned(boolean failed, boolean firstLogin);
    }

    public LoginScreenPresenter(Context context, Host host)
    {
        this.context = context;
        this.host = host;
        EventBusExt.getDefault().register(this);
    }

    public void runGoogleLogin(String token, String name, String imageURL, String coverURL)
    {
        Queues.networkQueue(context).execute(new UpdatedGoogleLoginTask(token, name, imageURL, coverURL));
    }

    public void onEventMainThread(UpdatedGoogleLoginTask task)
    {
        host.onLoginReturned(task.failed, task.firstLogin);
    }

    public void unregister()
    {
        EventBusExt.getDefault().unregister(this);
    }
}
