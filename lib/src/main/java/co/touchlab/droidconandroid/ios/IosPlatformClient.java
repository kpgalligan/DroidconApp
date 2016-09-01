package co.touchlab.droidconandroid.ios;
import android.util.Log;

import co.touchlab.droidconandroid.presenter.PlatformClient;
import retrofit.client.Client;

/**
 * Created by kgalligan on 4/10/16.
 */
public class IosPlatformClient implements PlatformClient
{
    @Override
    public Client makeClient()
    {
        return null;
    }

    @Override
    public String baseUrl()
    {
        return "https://droidcon-server.herokuapp.com/";
    }

    @Override
    public Integer getConventionId()
    {
        return 61100;
    }

    @Override
    public void log(String s)
    {
        Log.e("IosPlatformClient", s);
    }

    @Override
    public void logException(Throwable t)
    {
        Log.e("IosPlatformClient", "", t);
    }

    @Override
    public native String getString(String id)/*-[
    return [[NSBundle mainBundle] objectForInfoDictionaryKey:id_];
    ]-*/;
    /*{
        return null;
    }*/
}
