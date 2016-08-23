package co.touchlab.droidconandroid.tasks;
import co.touchlab.droidconandroid.network.WatchVideoRequest;
import retrofit.client.Response;

/**
 * Created by kgalligan on 8/17/16.
 */
public class CheckWatchVideoTask extends AbstractWatchVideoTask
{
    @Override
    Response callVideoUrl(WatchVideoRequest watchVideoRequest, String email, String uuid, long conventionId)
    {
        return watchVideoRequest.checkWatchVideo(conventionId, email, uuid);
    }
}
