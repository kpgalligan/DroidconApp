package co.touchlab.droidconandroid.network;

import java.util.List;

import retrofit.http.GET;

/**
 * Created by kgalligan on 7/20/14.
 */
public interface VoteRequest
{
    //    @FormUrlEncoded
    //    @POST("/dataTest/rsvpEvent/{eventId}")
    //    BasicIdResult addRsvp(@Path("eventId") Long eventId, @Field("rsvpUuid") String rsvpUuid);
    //
    //    @FormUrlEncoded
    //    @POST("/dataTest/unRsvpEvent/{eventId}")
    //    Response removeRsvp(@Path("eventId") Long eventId, @Field("dummy") String justFiller);


    @GET("/api/voter/voteSubmissions")
    List<TalkVotingWrapper> getTalkSubmission() throws Exception;
}
