package co.touchlab.droidconandroid.tasks;
import android.content.Context;

import co.touchlab.android.threading.tasks.Task;
import co.touchlab.droidconandroid.data.AppPrefs;
import co.touchlab.droidconandroid.data.DatabaseHelper;
import co.touchlab.droidconandroid.data.UserAccount;
import co.touchlab.squeaky.dao.Dao;

/**
 * Remove reference to the avatar image.  For use after uploading new avatar.
 */
public class QuickClearAvatarTask extends Task
{
    Long userId;

    public QuickClearAvatarTask(Long userId)
    {
        this.userId = userId;
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        Dao<UserAccount> dao = DatabaseHelper.getInstance(context).getUserAccountDao();
        UserAccount userAccount = dao.queryForId(userId);
        userAccount.avatarKey = null;
        AppPrefs.getInstance(context).setAvatarKey(null);
        dao.createOrUpdate(userAccount);
    }

    @Override
    protected boolean handleError(Context context, Throwable throwable)
    {
        return false;
    }
}
