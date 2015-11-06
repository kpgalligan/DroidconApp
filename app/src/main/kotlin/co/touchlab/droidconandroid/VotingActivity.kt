package co.touchlab.droidconandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.wnafee.vector.compat.ResourcesCompat

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
        setSupportActionBar(toolbar);
        toolbar!!.setBackgroundColor(resources.getColor(R.color.droidcon_green))

        val fragment = VoteFragment.newInstance()
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment, VoteFragment.Tag)
                .commit()

        //        EventBusExt.getDefault()!!.register(this)
        //        TaskQueue.loadQueueDefault(this).execute(FindVoteTaskKot())

    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        getMenuInflater().inflate(R.menu.vote_detail, menu)
//        val pass = menu!!.findItem(R.id.action_pass)
////        pass.setIcon(ResourcesCompat.getDrawable(this, R.drawable.ic_filter))
//        return super<AppCompatActivity>.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        when {
//            item!!.getItemId() == R.id.action_pass -> {
////                drawerLayout!!.openDrawer(findViewById(R.id.filter_wrapper))
//            }
////            item.getItemId() == R.id.action_search -> {
////                FindUserKot.startMe(this@MyActivity)
////            }
//        }
//        return super<AppCompatActivity>.onOptionsItemSelected(item)
//    }


    //    override fun onDestroy() {
    //        super.onDestroy()
    //        EventBusExt.getDefault()!!.unregister(this)
    //    }


    //----------EVENT------------------
    //    public fun onEventMainThread(t: FindVoteTaskKot) {
    //        initRvAdapter(t.list)
    //    }

}
