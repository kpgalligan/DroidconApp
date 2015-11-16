package co.touchlab.droidconandroid.network;
import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import co.touchlab.droidconandroid.data.AppPrefs;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;

/**
 * Created by samuelhill on 11/16/15.
 */
public class LoginServiceGenerator
{
    public static final String LOGIN_URL = "https://www.eventbrite.com/oauth";

    public static LoginService getService(Context context)
    {
        RestAdapter.Builder builder = makeRequestAdapterBuilder(context, null);
        return builder.build().create(LoginService.class);
    }

    @NotNull
    private static RestAdapter.Builder makeRequestAdapterBuilder(Context context, ErrorHandler errorHandler)
    {
        AppPrefs appPrefs = AppPrefs.getInstance(context);
        final String userUuid = appPrefs.getUserUuid();

        RequestInterceptor requestInterceptor = new RequestInterceptor()
        {
            @Override
            public void intercept(RequestFacade request)
            {
                request.addHeader("Accept", "application/json");
                if (! TextUtils.isEmpty(userUuid))
                    request.addHeader("uuid", userUuid);
            }
        };
        Gson gson = new GsonBuilder().create();
        GsonConverter gsonConverter = new GsonConverter(gson);

        final OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(15, TimeUnit.SECONDS);
        okHttpClient.setConnectTimeout(15, TimeUnit.SECONDS);

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setRequestInterceptor(requestInterceptor)
                .setConverter(gsonConverter)
                .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog("DroidconApp"))
                .setEndpoint(LOGIN_URL)
                .setClient(new OkClient(okHttpClient));

        if (errorHandler != null)
            builder.setErrorHandler(errorHandler);

        return builder;
    }

    public class AccessToken {

        private String accessToken;
        private String tokenType;

        public String getAccessToken() {
            return accessToken;
        }

        public String getTokenType() {
            // OAuth requires uppercase Authorization HTTP header value for token type
            if ( ! Character.isUpperCase(tokenType.charAt(0))) {
                tokenType =
                        Character
                                .toString(tokenType.charAt(0))
                                .toUpperCase() + tokenType.substring(1);
            }

            return tokenType;
        }
    }
}
