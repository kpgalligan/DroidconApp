package co.touchlab.droidconandroid.tasks.persisted;
import android.content.Context;
import android.util.Log;

import java.sql.SQLException;

import co.touchlab.android.threading.tasks.helper.RetrofitPersistedTask;
import co.touchlab.android.threading.tasks.persisted.PersistedTask;
import co.touchlab.droidconandroid.CrashReport;
import co.touchlab.droidconandroid.data.AppPrefs;
import co.touchlab.droidconandroid.data.DatabaseHelper;
import co.touchlab.droidconandroid.data.UserAccount;
import co.touchlab.droidconandroid.network.DataHelper;
import co.touchlab.droidconandroid.network.UpdateUserProfile;
import co.touchlab.droidconandroid.presenter.AppManager;
import retrofit.RestAdapter;

/**
 * Created by kgalligan on 4/8/16.
 */
public class UpdateUserProfileCommand extends RetrofitPersistedTask
{
    @Override
    protected void runNetwork(Context context)
    {
        AppPrefs appPrefs = AppPrefs.getInstance(context);
        if (appPrefs.isLoggedIn())
        {
            UserAccount userAccount = null;
            try
            {
                userAccount = DatabaseHelper.getInstance(context).getUserAccountDao().queryForId(appPrefs.getUserId());
            }
            catch(SQLException e)
            {
                throw new RuntimeException(e);
            }

            RestAdapter restAdapter = DataHelper.makeRequestAdapter(context,
                                                                    AppManager.getPlatformClient());
            UpdateUserProfile updateUserProfile = restAdapter.create(UpdateUserProfile.class);

            if (userAccount != null)
            {
                updateUserProfile.update(userAccount.name, userAccount.profile, userAccount.company,
                                         userAccount.twitter, userAccount.linkedIn,
                                         userAccount.website, null, null, userAccount.phone,
                                         userAccount.email, userAccount.gPlus, userAccount.facebook,
                                         userAccount.emailPublic);
            }
            else
            {
                throw new RuntimeException("User update failed");
            }
        }
    }

    @Override
    protected boolean handleError(Context context, Throwable e)
    {
        Log.e("User Profile", "Error while updating the user profile", e);
        CrashReport.logException(e);
        return true;
    }

    @Override
    protected boolean same(PersistedTask persistedTask)
    {
        return persistedTask instanceof UpdateUserProfileCommand;
    }

    @Override
    protected String logSummary()
    {
        return "";
    }
}
