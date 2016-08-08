package co.touchlab.droidconandroid.tasks;
import android.content.Context;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.Task;
import co.touchlab.droidconandroid.CrashReport;
import co.touchlab.droidconandroid.data.UserAccount;
import co.touchlab.droidconandroid.data.UserAuthHelper;
import co.touchlab.droidconandroid.network.GoogleLoginRequest;
import co.touchlab.droidconandroid.network.dao.LoginResult;
import co.touchlab.droidconandroid.presenter.AppManager;
import co.touchlab.droidconandroid.tasks.persisted.RefreshScheduleData;
import retrofit.RestAdapter;

/**
 * Created by kgalligan on 4/18/16.
 */
public class UpdatedGoogleLoginTask extends Task
{
    private final String token;
    private final String name;
    private final String imageURL;
    private final String coverURL;

    public boolean failed;
    public boolean firstLogin;

    public UpdatedGoogleLoginTask(String token, String name, String imageURL, String coverURL)
    {
        this.token = token;
        this.name = name;
        this.imageURL = imageURL;
        this.coverURL = coverURL;
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        /*RestAdapter restAdapter = DataHelper
                .makeRequestAdapter(context, PlatformClientContainer.platformClient);*/
        RestAdapter.Builder builder = new RestAdapter.Builder();

        builder.setEndpoint(AppManager.getPlatformClient().baseUrl());
        final RestAdapter restAdapter = builder.build();
//        final RefreshScheduleDataRequest refreshScheduleDataRequest = restAdapter
//                .create(RefreshScheduleDataRequest.class);

        GoogleLoginRequest loginRequest = restAdapter.create(GoogleLoginRequest.class);
        LoginResult loginResult = loginRequest.login(token, name);

        UserAccount userAccount = UserAuthHelper.processLoginResonse(context, loginResult);
        firstLogin = StringUtils.isEmpty(userAccount.profile) && StringUtils.isEmpty(userAccount.company);

        RefreshScheduleData.callMe(context);

        Log.w(UpdatedGoogleLoginTask.class.getSimpleName(), "Logged in! "+ userAccount.email);

//        if (! TextUtils.isEmpty(imageURL))
//            PersistedTaskQueueFactory.getInstance(context).execute(UploadAvatarCommand(imageURL));
//
//        if (!TextUtils.isEmpty(coverURL))
//            PersistedTaskQueueFactory.getInstance(context).execute(UploadCoverCommand(coverURL));
    }

    @Override
    protected boolean handleError(Context context, Throwable e)
    {
        CrashReport.logException(e);
        Log.w("GoogleLoginTask", "", e);
        failed = true;
        EventBusExt.getDefault().post(this);
        return true;
    }

    @Override
    protected void onComplete(Context context)
    {
        EventBusExt.getDefault().post(this);
    }
}
