package co.touchlab.droidconandroid;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.google.firebase.crash.FirebaseCrash;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import co.touchlab.droidconandroid.presenter.AppManager;
import co.touchlab.droidconandroid.presenter.PlatformClient;
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
//        Fabric.with(this, new Crashlytics());

//        if(!getCurrentProcessName(this).contains("background_crash"))
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
                    //                Crashlytics.logException(t);
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
}
