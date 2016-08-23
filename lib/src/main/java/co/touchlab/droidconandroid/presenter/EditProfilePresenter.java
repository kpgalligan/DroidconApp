package co.touchlab.droidconandroid.presenter;
import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Patterns;

import com.google.j2objc.annotations.Weak;

import co.touchlab.droidconandroid.data.AppPrefs;
import co.touchlab.droidconandroid.tasks.GrabUserProfile;
import co.touchlab.droidconandroid.tasks.Queues;
import co.touchlab.droidconandroid.tasks.QuickClearAvatarTask;
import co.touchlab.droidconandroid.tasks.UpdateUserProfileTask;
import co.touchlab.droidconandroid.tasks.persisted.PersistedTaskQueueFactory;
import co.touchlab.droidconandroid.tasks.persisted.UploadProfilePhotoTask;

/**
 * Created by Ramona Harrison
 * on 7/26/16.
 */

public class EditProfilePresenter extends AbstractEventBusPresenter
{
    private static final String VALIDATION_ERROR_NAME = "Don\'t forget your name!";
    private static final String VALIDATION_ERROR_PHONE = "Please enter a valid phone number.";
    private static final String VALIDATION_ERROR_EMAIL = "Please enter a valid email address.";

    private final long userId;
    private boolean isInitialDataSet;

    @Weak
    private EditProfileHost host;

    public EditProfilePresenter(Context context, EditProfileHost host, boolean isInitialDataSet)
    {
        super(context);
        this.userId = AppPrefs.getInstance(context).getUserId();
        this.host = host;
        this.isInitialDataSet = isInitialDataSet;
        refreshData();
    }

    public void refreshData()
    {
        Queues.localQueue(getContext()).execute(new GrabUserProfile(userId));
    }

    public void onEventMainThread(GrabUserProfile task)
    {
        if (!isInitialDataSet)
        {
            host.setUserAccount(task.userAccount);
            isInitialDataSet = true;
        }

        host.setProfilePhoto(task.userAccount.avatarImageUrl(), task.userAccount.name);
    }

    public void onEventMainThread(UploadProfilePhotoTask task)
    {
        refreshData();
    }

    public boolean saveProfile(String name, String bio, String company, String twitter, String linkedIn, String website, String facebook, String phone, String email, String gPlus, boolean showEmail)
    {
        if(TextUtils.isEmpty(name))
        {
            host.showMessage(VALIDATION_ERROR_NAME);
            return false;
        }
        else if(! TextUtils.isEmpty(phone) && ! PhoneNumberUtils.isGlobalPhoneNumber(phone))
        {
            host.showMessage(VALIDATION_ERROR_PHONE);
            return false;
        }
        else if(! TextUtils.isEmpty(email) && ! Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            host.showMessage(VALIDATION_ERROR_EMAIL);
            return false;
        }

        while (twitter.startsWith("@")) {
            twitter = twitter.substring(1);
        }

        Queues.localQueue(getContext())
                .execute(new UpdateUserProfileTask(name,
                        bio,
                        company,
                        twitter,
                        linkedIn,
                        website,
                        facebook,
                        phone,
                        email,
                        gPlus,
                        showEmail));

        return true;
    }

    public void uploadProfilePhoto(String path)
    {
        Queues.localQueue(getContext()).execute(new QuickClearAvatarTask(userId));
        PersistedTaskQueueFactory.getInstance(getContext())
                .execute(new UploadProfilePhotoTask(path, true));
    }
}