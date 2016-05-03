package co.touchlab.droidconandroid.tasks;
import android.content.Context;

import com.google.android.gms.auth.GoogleAuthUtil;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.Task;
import co.touchlab.droidconandroid.presenter.LoginScreenPresenter;

/**
 * Created by kgalligan on 4/29/16.
 */
public class RunGoogleLoginTask extends Task
{
    public static final String SCOPE = "audience:server:client_id:654878069390-ft2vt5sp4v0pcfk4poausabjnah0aeod.apps.googleusercontent.com";
    private final String               accountName;
    private final LoginScreenPresenter presenter;
    private final String               displayName;
    private final String               imageURL;
    private final String               coverURL;
    private String token;

    public RunGoogleLoginTask(String accountName, LoginScreenPresenter presenter, String displayName, String imageURL, String coverURL)
    {
        this.accountName = accountName;
        this.presenter = presenter;
        this.displayName = displayName;
        this.imageURL = imageURL;
        this.coverURL = coverURL;
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        token = GoogleAuthUtil.getToken(context, accountName, SCOPE);
    }

    @Override
    protected void onComplete(Context context)
    {
        presenter.runGoogleLogin(token, displayName, imageURL, coverURL);
        EventBusExt.getDefault().post(this);
    }

    @Override
    protected boolean handleError(Context context, Throwable e)
    {
        return false;
    }
}
