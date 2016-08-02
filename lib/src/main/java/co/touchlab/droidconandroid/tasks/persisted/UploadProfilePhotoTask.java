package co.touchlab.droidconandroid.tasks.persisted;
import android.content.Context;
import android.util.Log;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.helper.RetrofitPersistedTask;
import co.touchlab.android.threading.tasks.persisted.PersistedTask;
import co.touchlab.droidconandroid.presenter.AppManager;

/**
 * Created by Ramona Harrison
 * on 8/2/16.
 */

public class UploadProfilePhotoTask extends RetrofitPersistedTask
{
    private String imageUrl;

    public UploadProfilePhotoTask(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    @Override
    protected void runNetwork(Context context)
    {
        byte[] body;

        Log.d("ramona", "runNetwork: " + imageUrl);

//        if(imageUrl.startsWith("http"))
//        {
//            body = getImageFromUrl();
//        }
//        else
//        {
//            body = getImageFromFile();
//            Log.d("ramona", "runNetwork: " + body.length);
//        }

//        OkClient client = new OkClient();
//        List<Header> headers = new ArrayList<>();
//        headers.add(new Header("uuid", AppPrefs.getInstance(context).getUserUuid()));
//        TypedOutput typedBytes = new TypedByteArray("image/jpeg",  body);
//        Request request = new Request("POST", "url", headers, typedBytes);
//        Response response = client.execute(request);
//        Gson gson = new Gson();
//        UserInfoResponse userInfoResponse = gson.fromJson(response.getBody().)
//        AbstractFindUserTask.saveUserResponse(context, null, userInfoResponse);


        //        val userResponseString = uploadResponse?.getBodyAsString() ?: throw RuntimeException("No user response")
        //        val gson = Gson()
        //        val userInfoResponse = gson.fromJson(userResponseString, UserInfoResponse::class.java)
        //        AbstractFindUserTask.saveUserResponse(context!!, null, userInfoResponse!!)
        //        }

    }

    @Override
    protected void onComplete(Context context)
    {
        EventBusExt.getDefault().post(this);
    }

    @Override
    protected boolean handleError(Context context, Throwable e)
    {
        Log.w("asdf", "Whoops", e);
        AppManager.getPlatformClient().logException(e);
        return true;
    }

//    private byte[] getImageFromUrl()
//    {
//        byte[] body = {};
//
//        try
//        {
//            OkClient client = new OkClient();
//            Request request = new Request("GET", imageUrl, null, null);
//            Response response = client.execute(request);
//            body = ((TypedByteArray) response.getBody()).getBytes();
//        }
//        catch(Exception e)
//        {
//            // TODO review this exception
//            e.printStackTrace();
//        }
//
//        return body;
//    }
//
//    private byte[] getImageFromFile()
//    {
//        byte[] body = {};
//
//        try
//        {
//            FileInputStream input = new FileInputStream(imageUrl);
//            body = IOUtils.toByteArray(input);
//            input.close();
//        }
//        catch(Exception e)
//        {
//            // TODO review this exception
//            e.printStackTrace();
//        }
//
//        return body;
//    }

    @Override
    protected boolean same(PersistedTask persistedTask)
    {
        return false;
    }
}
