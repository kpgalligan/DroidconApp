package co.touchlab.droidconandroid.tasks.persisted;
import android.content.Context;
import android.util.Log;

import co.touchlab.android.threading.tasks.helper.RetrofitPersistedTask;
import co.touchlab.droidconandroid.CrashReport;
import co.touchlab.droidconandroid.PlatformClientContainer;
import co.touchlab.droidconandroid.network.BasicIdResult;
import co.touchlab.droidconandroid.network.DataHelper;
import co.touchlab.droidconandroid.network.RsvpRequest;

/**
 * Created by kgalligan on 8/21/15.
 */
public class AddRsvp extends RetrofitPersistedTask
{
    private Long eventId;
    private String rsvpUuid;

    public AddRsvp(){}

    public AddRsvp(Long eventId, String rsvpUuid)
    {
        this.eventId = eventId;
        this.rsvpUuid = rsvpUuid;
    }

    @Override
    protected void runNetwork(Context context)
    {
        RsvpRequest rsvpRequest = DataHelper.makeRequestAdapter(context, PlatformClientContainer.platformClient).create(RsvpRequest.class);
        if (eventId != null && rsvpUuid != null) {
            BasicIdResult basicIdResult = rsvpRequest.addRsvp(eventId, rsvpUuid);
            Log.w("asdf", "Result id: " + basicIdResult.id);
        } else {
            throw new IllegalArgumentException("Some value is null: " + eventId + "/" + rsvpUuid);
        }
    }

    @Override
    protected boolean handleError(Context context, Throwable e)
    {
        Log.e("RSVP", "Error adding RSVP eventID: " + eventId , e);
        CrashReport.logException(e);
        return true;
    }
}
