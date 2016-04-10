package co.touchlab.droidconandroid;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import co.touchlab.droidconandroid.data.AppPrefs;
import co.touchlab.droidconandroid.tasks.Queues;
import co.touchlab.droidconandroid.tasks.SeedScheduleDataTask;
import co.touchlab.droidconandroid.tasks.persisted.RefreshScheduleData;

import co.touchlab.droidconandroid.BuildConfig;
import co.touchlab.droidconandroid.R;

import io.fabric.sdk.android.Fabric;
import retrofit.client.Client;

/**
 * Created by kgalligan on 6/28/14.
 */
public class DroidconApplication extends Application
{
    public static final String FIRST_SEED = "FIRST_SEED";

    @Override
    public void onCreate()
    {
        try
        {
            super.onCreate();
            Fabric.with(this, new Crashlytics());

            PlatformClientContainer.platformClient = new PlatformClient()
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

            if(AppPrefs.getInstance(this).once(FIRST_SEED))
            {
    //            getAssets().open("dataseed.json")
                final String seed = IOUtils.toString(getAssets().open("dataseed.json"));
                Queues.localQueue(this).execute(new SeedScheduleDataTask(seed));
            }

            if(AppPrefs.getInstance(this).isLoggedIn())
                RefreshScheduleData.callMe(this);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }


    }
}
