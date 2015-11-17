package co.touchlab.droidconandroid.tasks.persisted;
import android.content.Context;

import com.crashlytics.android.Crashlytics;

import co.touchlab.android.threading.tasks.persisted.PersistedTask;
import co.touchlab.droidconandroid.utils.Toaster;
import retrofit.RetrofitError;

/**
 * Created by toidiu on 11/12/15.
 */
abstract public class BasePersistedTask extends PersistedTask
{

    protected abstract String errorString();

    @Override
    protected final boolean handleError(Context context, Throwable e)
    {
        if(e instanceof RetrofitError)
        {
            return true;
        }
        else
        {
            Crashlytics.logException(e);
            return true;
        }
    }
}
