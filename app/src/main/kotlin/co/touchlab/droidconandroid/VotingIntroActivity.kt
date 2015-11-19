package co.touchlab.droidconandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import co.touchlab.droidconandroid.data.AppPrefs
import co.touchlab.droidconandroid.data.DatabaseHelper
import co.touchlab.droidconandroid.tasks.persisted.GetTalkSubmissionPersisted
import co.touchlab.droidconandroid.tasks.persisted.PersistedTaskQueueFactory
import co.touchlab.droidconandroid.ui.DrawerAdapter
import co.touchlab.droidconandroid.ui.DrawerClickListener
import co.touchlab.droidconandroid.ui.NavigationItem
import co.touchlab.droidconandroid.utils.TimeUtils
import java.util.*

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
