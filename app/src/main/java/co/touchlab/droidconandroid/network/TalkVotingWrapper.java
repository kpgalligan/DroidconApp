package co.touchlab.droidconandroid.network;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import co.touchlab.droidconandroid.data.TalkSubmission;

/**
 * Created by toidiu on 11/9/15.
 */
public class TalkVotingWrapper
{

    public NetTalkSub talkSubmission;
    public NetVoteSub votingSubmission;

    public static class NetTalkSub
    {
        public Long     id;
        public String   title;
        public String   description;
        public UserName userAccount;
    }

    public static class UserName
    {
        public String name;
    }

    public static class NetVoteSub
    {
        public int vote;
    }

    public static List<TalkSubmission> parseResp(List<TalkVotingWrapper> resp)
    {
        List<TalkSubmission> list = new ArrayList<>();

        for(TalkVotingWrapper w : resp)
        {
            NetTalkSub t = w.talkSubmission;
            NetVoteSub v = w.votingSubmission;

            Integer vote = (v == null)
                    ? null
                    : v.vote;

            list.add(new TalkSubmission(t.id, vote, t.title, t.description, t.userAccount.name));
        }

        return list;
    }

}
