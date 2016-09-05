package co.touchlab.droidconandroid.tasks;
import co.touchlab.droidconandroid.network.WatchVideoRequest;
import co.touchlab.droidconandroid.presenter.AppManager;
import co.touchlab.droidconandroid.utils.AnalyticsEvents;
import retrofit.client.Response;

/**
 * Created by kgalligan on 8/17/16.
 */
public class StartWatchVideoTask extends AbstractWatchVideoTask
{
    private final long eventId;
    public final String link;
    public final String cover;

    public StartWatchVideoTask(long eventId, String link, String cover)
    {
        this.eventId = eventId;
        this.link = link;
        this.cover = cover;
    }

    @Override
    Response callVideoUrl(WatchVideoRequest watchVideoRequest, String email, String uuid, long conventionId)
    {
//        if(email != null)
//            throw new UnsupportedOperationException("Hey, test");
        AppManager.getPlatformClient().logEvent(AnalyticsEvents.START_VIDEO, "eventId", Long.toString(eventId));
        return watchVideoRequest.startWatchVideo(conventionId, email, uuid);
    }
}
