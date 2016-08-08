package co.touchlab.droidconandroid.presenter;
import co.touchlab.droidconandroid.data.UserAccount;

/**
 * Created by Ramona Harrison
 * on 7/26/16.
 */

public interface EditProfileHost
{
    void setUserAccount(UserAccount ua);

    void setProfilePhoto(String photoUrl, String name);

    void showMessage(String msg);
}
