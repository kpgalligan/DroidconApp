package co.touchlab.droidconandroid.tasks;
import co.touchlab.droidconandroid.network.WatchVideoRequest;
import retrofit.client.Response;

/**
 * Created by kgalligan on 8/17/16.
 */
public class StartWatchVideoTask extends AbstractWatchVideoTask
{
    public final String link;
    public final String cover;

    public StartWatchVideoTask(String link, String cover)
    {
        this.link = link;
        this.cover = cover;
    }

    @Override
    Response callVideoUrl(WatchVideoRequest watchVideoRequest, String email, String uuid, long conventionId)
    {
        return watchVideoRequest.startWatchVideo(conventionId, email, uuid);
    }
}
