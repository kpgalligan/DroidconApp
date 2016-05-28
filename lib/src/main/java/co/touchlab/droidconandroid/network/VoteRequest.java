package co.touchlab.droidconandroid.network;

import java.util.List;

import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by kgalligan on 7/20/14.
 */
public interface VoteRequest
{
    @FormUrlEncoded
    @POST("/api/voter/updateVote/convention/{cId}")
    Response updateVote(@Path("cId") int cId, @Field("id") Long talkId, @Field("vote") Integer vote) throws NetworkErrorHandler.NetworkException;

    @GET("/api/voter/voteSubmissions/{cId}")
    List<TalkVotingWrapper> getTalkSubmission(@Path("cId") int cId) throws NetworkErrorHandler.NetworkException;

    @GET("/api/voter/canUserVote/{conID}")
    Boolean canUserVote(@Path("conID") Integer conventionID) throws NetworkErrorHandler.NetworkException;

    @GET("/api/voter/canEBUserVote/{conID}/{authCode}")
    Response canEBUserVote(@Path("conID") Integer conventionID, @Path("authCode") String authCode) throws NetworkErrorHandler.NetworkException;
}
