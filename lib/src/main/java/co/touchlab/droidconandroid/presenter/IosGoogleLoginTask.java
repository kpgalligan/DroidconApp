package co.touchlab.droidconandroid.presenter;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.Task;
import co.touchlab.droidconandroid.PlatformClientContainer;
import co.touchlab.droidconandroid.data.UserAccount;
import co.touchlab.droidconandroid.data.UserAuthHelper;
import co.touchlab.droidconandroid.network.DataHelper;
import co.touchlab.droidconandroid.network.GoogleLoginRequest;
import co.touchlab.droidconandroid.network.dao.LoginResult;
import co.touchlab.droidconandroid.tasks.persisted.RefreshScheduleData;
import retrofit.RestAdapter;

/**
 * Created by kgalligan on 4/18/16.
 */
public class IosGoogleLoginTask extends Task
{
    private final String token;
    private final String name;
    public boolean firstLogin;

    public IosGoogleLoginTask(String token, String name)
    {
        this.token = token;
        this.name = name;
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        /*RestAdapter restAdapter = DataHelper
                .makeRequestAdapter(context, PlatformClientContainer.platformClient);*/
        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setEndpoint("https://droidcon-server.herokuapp.com/");
        final RestAdapter restAdapter = builder.build();
//        final RefreshScheduleDataRequest refreshScheduleDataRequest = restAdapter
//                .create(RefreshScheduleDataRequest.class);

        GoogleLoginRequest loginRequest = restAdapter.create(GoogleLoginRequest.class);
        LoginResult loginResult = loginRequest.login(token, name);

        UserAccount userAccount = UserAuthHelper.processLoginResonse(context, loginResult);
        firstLogin = StringUtils.isEmpty(userAccount.profile) && StringUtils.isEmpty(userAccount.company);

        RefreshScheduleData.callMe(context);

        Log.w(IosGoogleLoginTask.class.getSimpleName(), "Logged in! "+ userAccount.email);

//        if (! TextUtils.isEmpty(imageURL))
//            PersistedTaskQueueFactory.getInstance(context).execute(UploadAvatarCommand(imageURL!!))
//
//        if (!TextUtils.isEmpty(coverURL))
//            PersistedTaskQueueFactory.getInstance(context).execute(UploadCoverCommand(coverURL!!))
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
