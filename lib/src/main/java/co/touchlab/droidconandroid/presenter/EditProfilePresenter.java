package co.touchlab.droidconandroid.presenter;
import android.content.Context;

import com.google.j2objc.annotations.Weak;

import co.touchlab.droidconandroid.data.AppPrefs;
import co.touchlab.droidconandroid.tasks.GrabUserProfile;
import co.touchlab.droidconandroid.tasks.Queues;
import co.touchlab.droidconandroid.tasks.UpdateUserProfileTask;

/**
 * Created by Ramona Harrison
 * on 7/26/16.
 */

public class EditProfilePresenter extends AbstractEventBusPresenter
{
    private final long userId;

    @Weak
    private EditProfileHost host;

    public EditProfilePresenter(Context context, EditProfileHost host)
    {
        super(context);
        this.userId = AppPrefs.getInstance(context).getUserId();
        this.host = host;
        refreshData();
    }

    private void refreshData()
    {
        Queues.localQueue(getContext()).execute(new GrabUserProfile(userId));
    }

    public void onEventMainThread(GrabUserProfile task)
    {
        host.dataRefresh(task.userAccount);
    }

    public void saveProfile(String name, String bio, String company, String twitter, String linkedIn, String website, String facebook, String phone, String email, String gPlus, boolean showEmail)
    {
        Queues.localQueue(getContext()).execute(
                new UpdateUserProfileTask(name,
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
    }
}
