package co.touchlab.droidconandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import co.touchlab.droidconandroid.data.AppPrefs

/**
 *
 * Created by toidiu on 7/23/15.
 */
public class VotingIntroActivity : AppCompatActivity(), VoteIntroFragment.OnIntroListener, VoteAuthFragment.OnAuthListener {

    public companion object {
        public fun callMe(c: Context) {
            val i = Intent(c, VotingIntroActivity::class.java)
            c.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote_intro);

        if (savedInstanceState == null) {
            if (!AppPrefs.getInstance(this).getSeenVoteIntro()) {
                supportFragmentManager
                        .beginTransaction()
                        .add(R.id.fragContainer, VoteIntroFragment.newInstance(), VoteIntroFragment.Tag)
                        .commit()
            } else if (AppPrefs.getInstance(this).canUserVote()) {
                startVoting()
            }
            else
            {
                supportFragmentManager
                        .beginTransaction()
                        .add(R.id.fragContainer, VoteAuthFragment.newInstance(), VoteAuthFragment.Tag)
                        .commit()
            }
        }
    }

    override fun onIntroDone() {
        AppPrefs.getInstance(this).setSeenVoteIntro(true)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragContainer, VoteAuthFragment.newInstance(), VoteAuthFragment.Tag)
                .commit()
    }


    override fun onAuthSuccessful() {
        AppPrefs.getInstance(this).setCanUserVote(true);
        startVoting()
    }

    private fun startVoting() {
        VotingActivity.callMe(this);
        finish();
    }
}
