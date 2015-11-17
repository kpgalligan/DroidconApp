package co.touchlab.droidconandroid.network;

import java.util.List;

import co.touchlab.droidconandroid.data.AppPrefs;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by kgalligan on 7/20/14.
 */
public interface VoteRequest
{

    @FormUrlEncoded
    @POST("/api/voter/updateVote/{subId}")
    Response updateVote(@Path("subId") Long talkId, @Field("vote") Integer vote) throws NetworkErrorHandler.NetworkException;


    @GET("/api/voter/voteSubmissions")
    List<TalkVotingWrapper> getTalkSubmission() throws NetworkErrorHandler.NetworkException;

    @GET("/api/voter/canUserVote")
    Boolean canUserVote() throws NetworkErrorHandler.NetworkException;
}
