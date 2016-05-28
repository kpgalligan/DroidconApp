package co.touchlab.droidconandroid.presenter;
import android.content.Context;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import co.touchlab.droidconandroid.data.AppPrefs;
import co.touchlab.droidconandroid.tasks.Queues;
import co.touchlab.droidconandroid.tasks.SeedScheduleDataTask;
import co.touchlab.droidconandroid.tasks.persisted.RefreshScheduleData;
import co.touchlab.droidconandroid.utils.TimeUtils;

/**
 * Created by kgalligan on 4/19/16.
 */
public class AppManager
{
    public static final String FIRST_SEED = "FIRST_SEED";

    private static Context context;
    private static PlatformClient platformClient;

    public interface LoadDataSeed
    {
        String dataSeed();
    }

    public static void initContext(Context context, PlatformClient platformClient, LoadDataSeed loadDataSeed)
    {
        AppManager.context = context;
        AppManager.platformClient = platformClient;

        if(AppPrefs.getInstance(context).once(FIRST_SEED))
        {
            //            getAssets().open("dataseed.json")
            final String seed = loadDataSeed.dataSeed();
            Queues.localQueue(context).execute(new SeedScheduleDataTask(seed));
        }

//        if(AppPrefs.getInstance(context).isLoggedIn())
//            RefreshScheduleData.callMe(context);
    }

    public static Context getContext()
    {
        return context;
    }

    public static PlatformClient getPlatformClient()
    {
        return platformClient;
    }

    public enum AppScreens
    {
        Welcome, Login, Schedule, Voting
    }

    public static AppScreens findStartScreen(String votingEndsString)
    {
        final AppPrefs appPrefs = AppPrefs.getInstance(context);
        if (! appPrefs.getHasSeenWelcome())
        {
            return AppScreens.Welcome;
        }
        else if (!appPrefs.isLoggedIn())
        {
            return AppScreens.Login;
        }
        else if ( isVotingOpen(votingEndsString) )
        {
            return AppScreens.Voting;
        }
        else
        {
            return AppScreens.Schedule;
        }
    }

    public static boolean isVotingOpen(String votingEndsString)
    {
        if(StringUtils.isEmpty(votingEndsString))
            return false;

        try
        {
            Date votingEnd = TimeUtils.DATE_FORMAT.get().parse(votingEndsString);
            Calendar now = Calendar.getInstance();

            return now.before(votingEnd);
        }
        catch(ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static AppPrefs getAppPrefs()
    {
        return AppPrefs.getInstance(context);
    }
}
