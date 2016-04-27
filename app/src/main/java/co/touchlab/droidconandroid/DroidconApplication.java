package co.touchlab.droidconandroid;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import co.touchlab.droidconandroid.presenter.AppManager;
import co.touchlab.droidconandroid.presenter.PlatformClient;
import io.fabric.sdk.android.Fabric;
import retrofit.client.Client;

/**
 * Created by kgalligan on 6/28/14.
 */
public class DroidconApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        PlatformClient platformClient = new co.touchlab.droidconandroid.presenter.PlatformClient()
        {
            @Override
            public Client makeClient()
            {
                return null;
            }

            @Override
            public String baseUrl()
            {
                return BuildConfig.BASE_URL;
            }

            @Override
            public Integer getConventionId()
            {
                return Integer.parseInt(DroidconApplication.this.getString(R.string.convention_id));
            }

            @Override
            public void logException(Throwable t)
            {
                Crashlytics.logException(t);
            }

            @Override
            public String getString(String id)
            {
                return DroidconApplication.this
                        .getString(getResources().getIdentifier(id, "string", getPackageName()));
            }
        };

        AppManager.initContext(this, platformClient, new AppManager.LoadDataSeed()
        {
            @Override
            public String dataSeed()
            {
                try
                {
                    return IOUtils.toString(getAssets().open("dataseed.json"));
                }
                catch(IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
