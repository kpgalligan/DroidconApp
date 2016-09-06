package co.touchlab.droidconandroid;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.droidconandroid.alerts.AlertManagerKt;
import co.touchlab.droidconandroid.presenter.AppManager;
import co.touchlab.droidconandroid.presenter.PlatformClient;
import co.touchlab.droidconandroid.tasks.UpdateAlertsTask;
import retrofit.client.Client;

/**
 * Created by kgalligan on 6/28/14.
 */
public class DroidconApplication extends Application
{
    public static String getCurrentProcessName(Context context) {
        // Log.d(TAG, "getCurrentProcessName");
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses())
        {
            // Log.d(TAG, processInfo.processName);
            if (processInfo.pid == pid)
                return processInfo.processName;
        }
        return "";
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        EventBusExt.getDefault().register(this);

        String currentProcessName = getCurrentProcessName(this);
        Log.i(DroidconApplication.class.getSimpleName(), "currentProcessName: "+ currentProcessName );
        if(!currentProcessName.contains("background_crash"))
        {
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
                public void log(String s)
                {
                    FirebaseCrash.log(s);
                }

                @Override
                public void logException(Throwable t)
                {
                    FirebaseCrash.report(t);
                }

                @Override
                public void logEvent(String name, String... params)
                {
                    Bundle bundle = new Bundle();
                    for(int i=0; i<params.length; )
                    {
                        bundle.putString(params[i], params[i+1]);
                        i= i+2;
                    }
                    FirebaseAnalytics.getInstance(DroidconApplication.this)
                            .logEvent(name, bundle);
                }

                @Override
                public String getString(String id)
                {
                    return DroidconApplication.this.getString(
                            getResources().getIdentifier(id, "string", getPackageName()));
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

    @SuppressWarnings("unused")
    public void onEventMainThread(UpdateAlertsTask task)
    {
        AlertManagerKt.scheduleAlert(this, task.nextEvent);
    }
}
