package co.touchlab.droidconandroid.tasks.persisted;
import android.content.Context;

import co.touchlab.android.threading.errorcontrol.NetworkException;
import co.touchlab.android.threading.tasks.persisted.PersistedTask;
import retrofit.RetrofitError;

/**
 * Created by toidiu on 11/12/15.
 */
abstract public class BasePersistedTask extends PersistedTask
{

    @Override
    protected final void run(Context context) throws Throwable
    {
        try
        {
            runNetwork(context);
        }
        catch(RetrofitError e)
        {
            if(e.getKind() == RetrofitError.Kind.NETWORK)
            {
                throw new NetworkException(e);
            }
        }
    }

    protected abstract void runNetwork(Context context) throws Throwable;

    @Override
    protected boolean handleError(Context context, Throwable e)
    {
        return false;
    }
}
