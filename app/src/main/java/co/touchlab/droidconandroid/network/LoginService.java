package co.touchlab.droidconandroid.network;
import co.touchlab.android.threading.errorcontrol.NetworkException;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by samuelhill on 11/16/15.
 */
public interface LoginService
{
    @POST("/token")
    LoginServiceGenerator.AccessToken getAccessToken(
            @Query("code") String code,
            @Query("grant_type") String grantType) throws NetworkException;
}
