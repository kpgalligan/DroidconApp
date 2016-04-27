package co.touchlab.droidconandroid.presenter;
import retrofit.client.Client;

/**
 * Created by kgalligan on 4/6/16.
 */
public interface PlatformClient
{
    Client makeClient();
    String baseUrl();
    Integer getConventionId();
    void logException(Throwable t);
    String getString(String id);
}
