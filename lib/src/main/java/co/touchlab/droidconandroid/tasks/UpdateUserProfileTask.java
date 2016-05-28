package co.touchlab.droidconandroid.tasks;
import android.content.Context;

import java.sql.SQLException;

import co.touchlab.android.threading.tasks.Task;
import co.touchlab.droidconandroid.data.AppPrefs;
import co.touchlab.droidconandroid.data.DatabaseHelper;
import co.touchlab.droidconandroid.data.UserAccount;
import co.touchlab.droidconandroid.tasks.persisted.PersistedTaskQueueFactory;
import co.touchlab.droidconandroid.tasks.persisted.UpdateUserProfileCommand;
import co.touchlab.squeaky.dao.Dao;

/**
 * Created by kgalligan on 4/8/16.
 */
public class UpdateUserProfileTask extends Task
{
    final String name;
    final String profile;
    final String company;
    final String twitter;
    final String linkedIn;
    final String website;
    final String facebook;
    final String phone;
    final String email;
    final String gPlus;
    final boolean emailPublic;

    public UpdateUserProfileTask(String name, String profile, String company, String twitter, String linkedIn, String website, String facebook, String phone, String email, String gPlus, boolean emailPublic)
    {
        this.name = name;
        this.profile = profile;
        this.company = company;
        this.twitter = twitter;
        this.linkedIn = linkedIn;
        this.website = website;
        this.facebook = facebook;
        this.phone = phone;
        this.email = email;
        this.gPlus = gPlus;
        this.emailPublic = emailPublic;
    }

    @Override
    protected void run(final Context context) throws Throwable
    {
        final AppPrefs appPrefs = AppPrefs.getInstance(context);
        if (appPrefs.isLoggedIn())
        {
            DatabaseHelper.getInstance(context).inTransaction(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Dao<UserAccount> dao = DatabaseHelper.getInstance(context).getUserAccountDao();
                        UserAccount userAccount = dao.queryForId(appPrefs.getUserId());
                        userAccount.name = name;
                        userAccount.profile = profile;
                        userAccount.company = company;
                        userAccount.twitter = twitter;
                        userAccount.linkedIn = linkedIn;
                        userAccount.website = website;
                        userAccount.facebook = facebook;
                        userAccount.phone = phone;
                        userAccount.email = email;
                        userAccount.gPlus = gPlus;
                        userAccount.emailPublic = emailPublic;
                        dao.createOrUpdate(userAccount);

                        AppPrefs.getInstance(context).setName(name);
                        AppPrefs.getInstance(context).setEmail(email);
                        PersistedTaskQueueFactory.getInstance(context).execute(new UpdateUserProfileCommand());
                    }
                    catch(SQLException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            });
            
            
        }
    }

    @Override
    protected boolean handleError(Context context, Throwable e)
    {
        return false;
    }
}
