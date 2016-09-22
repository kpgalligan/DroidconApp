package co.touchlab.droidconandroid.tasks.persisted;
import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import co.touchlab.android.threading.errorcontrol.SoftException;
import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.persisted.PersistedTask;
import co.touchlab.droidconandroid.data.AppPrefs;
import co.touchlab.droidconandroid.presenter.AppManager;

/**
 * Created by Ramona Harrison
 * on 8/2/16.
 */

public class UploadProfilePhotoTask extends PersistedTask
{
    private static final String TAG                   = "UploadProfilePhotoTask";
    private static final String ENDPOINT_PATH_DEFAULT = "dataTest/uploadAvatar";
    private static final String ENDPOINT_PATH_FORCE   = "dataTest/uploadAvatarForce";
    private static final int    MAX_BUFFER_SIZE       = 12288;

    private String imageUrl;
    private String endpoint;

    public UploadProfilePhotoTask()
    {
    }

    public UploadProfilePhotoTask(String imageUrl, boolean force)
    {
        this.imageUrl = imageUrl;

        // Default endpoint only accepts the avatar image if there isn't one already (e.g. first login w/ google).
        if (force)
        {
            this.endpoint = AppManager.getPlatformClient().baseUrl() + ENDPOINT_PATH_FORCE;
        }
        else
        {
            this.endpoint = AppManager.getPlatformClient().baseUrl() + ENDPOINT_PATH_DEFAULT;
        }
    }

    @Override
    protected void run(Context context) throws SoftException, Throwable
    {
        // Setup the request
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "image/jpeg");
        connection.setRequestProperty("uuid", AppPrefs.getInstance(context).getUserUuid());

        // Upload the image
        InputStream in = getInputStream(imageUrl);
        OutputStream out = connection.getOutputStream();
        copy(in, out);

        // Log the server response
        int serverResponseCode = connection.getResponseCode();
        String serverResponseMessage = connection.getResponseMessage();
        AppManager.getPlatformClient().log("Server response: " + serverResponseCode + ": " + serverResponseMessage);

        // Close the streams
        in.close();
        out.flush();
        out.close();
    }

    @Override
    protected void onComplete(Context context)
    {
        EventBusExt.getDefault().post(this);
    }

    @Override
    protected boolean handleError(Context context, Throwable e)
    {
        AppManager.getPlatformClient().logException(e);
        return true;
    }

    private InputStream getInputStream(String imageUrl) throws Exception
    {
        byte[] body;

        if(imageUrl.startsWith("http"))
        {
            URLConnection connection = (new URL(imageUrl)).openConnection();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            copy(in, out);

            in.close();
            body = out.toByteArray();
        }
        else
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            FileInputStream in = new FileInputStream(imageUrl);

            copy(in, out);

            in.close();
            body = out.toByteArray();
        }

        return new ByteArrayInputStream(body);
    }


    private void copy(InputStream in, OutputStream out) throws IOException
    {
        int bytesAvailable = in.available();
        int bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);

        int n;
        byte[] buffer = new byte[bufferSize];
        while((n = in.read(buffer)) > 0)
        {
            out.write(buffer, 0, n);
        }
    }
}