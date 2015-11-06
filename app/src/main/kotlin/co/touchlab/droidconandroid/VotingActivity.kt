package co.touchlab.droidconandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

/**
 *
 * Created by toidiu on 7/23/15.
 */
public class VotingActivity : AppCompatActivity() {

    public companion object {
        public fun getLaunchIntent(c: Context): Intent =  Intent(c, javaClass<VotingActivity>())

    }

    var toolbar: Toolbar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote);

        toolbar = findViewById(R.id.toolbar) as Toolbar;
        toolbar!!.setTitle(R.string.vote)
        setSupportActionBar(toolbar);
        toolbar!!.setBackgroundColor(getResources().getColor(R.color.droidcon_green))

        val fragment = VoteFragment.newInstance()
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .commit()

        //        EventBusExt.getDefault()!!.register(this)
        //        TaskQueue.loadQueueDefault(this).execute(FindVoteTaskKot())

    }

    //    override fun onDestroy() {
    //        super.onDestroy()
    //        EventBusExt.getDefault()!!.unregister(this)
    //    }


    //----------EVENT------------------
    //    public fun onEventMainThread(t: FindVoteTaskKot) {
    //        initRvAdapter(t.list)
    //    }

}
