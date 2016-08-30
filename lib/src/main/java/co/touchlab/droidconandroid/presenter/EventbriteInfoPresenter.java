package co.touchlab.droidconandroid.presenter;
import android.content.Context;
import android.text.TextUtils;

import com.google.j2objc.annotations.Weak;

import co.touchlab.droidconandroid.data.AppPrefs;

/**
 * This is kind of overkill, but keeping up the pattern.
 *
 * Created by kgalligan on 8/30/16.
 */
public class EventbriteInfoPresenter
{
    @Weak
    private final EventbriteInfoHost host;

    public EventbriteInfoPresenter(EventbriteInfoHost host)
    {
        this.host = host;
        String currentEmail = AppPrefs.getInstance(host.getContext()).getEventbriteEmail();
        host.setEmail(currentEmail == null ? "" : currentEmail.trim());
    }

    public void updateEmail()
    {
        AppPrefs.getInstance(host.getContext()).setEventbriteEmail(host.getEmail());
    }
}
