package co.touchlab.droidconandroid.data;
import android.content.Context;

import java.sql.SQLException;

import co.touchlab.droidconandroid.network.dao.LoginResult;

/**
 * Created by kgalligan on 4/7/16.
 */
public class UserAuthHelper
{
    public static UserAccount processLoginResonse(Context c, LoginResult result) throws SQLException
    {
        UserAccount newDbUser = new UserAccount();
        userAccountToDb(result.user, newDbUser);

        DatabaseHelper.getInstance(c).getUserAccountDao().createOrUpdate(newDbUser);

        //Save db first, then these.
        AppPrefs appPrefs = AppPrefs.getInstance(c);
        appPrefs.setUserUuid(result.uuid);
        appPrefs.setUserId(result.userId);

        saveDrawerAppPrefs(c, newDbUser);

        return newDbUser;
    }

    public static void userAccountToDb(co.touchlab.droidconandroid.network.dao.UserAccount ua, co.touchlab.droidconandroid.data.UserAccount dbUa)
    {
        dbUa.id = ua.id;
        dbUa.name = ua.name;
        dbUa.profile = ua.profile;
        dbUa.avatarKey = ua.avatarKey;
        dbUa.userCode = ua.userCode;
        dbUa.company = ua.company;
        dbUa.twitter = ua.twitter;
        dbUa.linkedIn = ua.linkedIn;
        dbUa.website = ua.website;
        dbUa.following = ua.following;
        dbUa.gPlus = ua.gPlus;
        dbUa.phone = ua.phone;
        dbUa.email = ua.email;
        dbUa.coverKey = ua.coverKey;
        dbUa.facebook = ua.facebook;
        dbUa.emailPublic = ua.emailPublic;
    }

    public static void saveDrawerAppPrefs(Context context, UserAccount user) {
        AppPrefs appPrefs = AppPrefs.getInstance(context);
        appPrefs.setAvatarKey(user.avatarKey);
        appPrefs.setCoverKey(user.coverKey);
        appPrefs.setName(user.name);
        appPrefs.setEmail(user.email);
    }
}