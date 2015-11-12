package co.touchlab.droidconandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import co.touchlab.android.threading.tasks.TaskQueue
import co.touchlab.droidconandroid.tasks.persisted.GetTalkSubmissionPersisted
import co.touchlab.droidconandroid.tasks.persisted.PersistedTaskQueueFactory
import co.touchlab.droidconandroid.tasks.persisted.UpdateVotePersisted

/**
 *
 * Created by toidiu on 7/23/15.
 */
public class VotingActivity : AppCompatActivity() {

    public companion object {
        public fun getLaunchIntent(c: Context): Intent = Intent(c, javaClass<VotingActivity>())

    }

    var toolbar: Toolbar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote);

        toolbar = findViewById(R.id.toolbar) as Toolbar;
        setSupportActionBar(toolbar);
        toolbar!!.setBackgroundColor(resources.getColor(R.color.droidcon_green))

        val fragment = VoteFragment.newInstance()
        supportFragmentManager
                .beginTransaction()
                .add(R.id.container, fragment, VoteFragment.Tag)
                .commit()

        PersistedTaskQueueFactory.getInstance(this).execute(GetTalkSubmissionPersisted())
    }

}
