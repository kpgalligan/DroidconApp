package co.touchlab.droidconandroid

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view
import android.view.*
import android.widget.Toast
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.android.threading.tasks.TaskQueue
import co.touchlab.droidconandroid.data.Event
import co.touchlab.droidconandroid.tasks.FindVoteTaskKot
import co.touchlab.droidconandroid.ui.VoteAdapter
import co.touchlab.droidconandroid.ui.VoteClickListener
import com.wnafee.vector.compat.ResourcesCompat


/**
 *
 * Created by toidiu on 8/5/15.
 */
class VoteFragment : Fragment() {

    var rv: RecyclerView? = null
    var adapter: VoteAdapter? = null

    companion object {
        val Tag: String = "VoteFragment"

        fun newInstance(): VoteFragment = VoteFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: view.ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_vote, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Fragment>.onCreate(savedInstanceState)
        EventBusExt.getDefault().register(this)
        setHasOptionsMenu(true);
    }


    override fun onDestroy() {
        super<Fragment>.onDestroy()
        EventBusExt.getDefault().unregister(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super<Fragment>.onActivityCreated(savedInstanceState)

        rv = view.findViewById(R.id.rv) as RecyclerView
        rv!!.setLayoutManager(LinearLayoutManager(getActivity()))

        (activity as AppCompatActivity).supportActionBar.setTitle(R.string.vote)

        TaskQueue.loadQueueDefault(activity).execute(FindVoteTaskKot())
    }
//
//    private fun initListeners() {
//        val supportFragmentManager = (activity as AppCompatActivity).supportFragmentManager
//        supportFragmentManager.addOnBackStackChangedListener {
//
//            if (supportFragmentManager.findFragmentByTag(Tag) != null)
//                (activity as AppCompatActivity).supportActionBar.setTitle(R.string.vote)
//        }
//
//    }

    fun initRvAdapter(data: List<Event>) {
        adapter = VoteAdapter(data, object : VoteClickListener {
            override fun onEventClick(event: Event) {
                val fragment = VoteDetailFragment.newInstance(event.id)

                fragment.show(activity.supportFragmentManager, null)
//                activity.supportFragmentManager
//                        .beginTransaction()
//                        .add(R.id.container, fragment)
//                        .addToBackStack(null)
//                        .commit()
            }
        })

        rv!!.setAdapter(adapter!!)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        Log.d("ak-----------", "--------2");

        inflater!!.inflate(R.menu.vote_list, menu)
        val votedList = menu!!.findItem(R.id.action_voted)
        votedList.setIcon(ResourcesCompat.getDrawable(activity, R.drawable.ic_voted))
        return super<Fragment>.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when {
            item!!.itemId == R.id.action_voted -> {

            }
        }
        return super<Fragment>.onOptionsItemSelected(item)
    }

    //----------EVENT------------------
    public fun onEventMainThread(t: FindVoteTaskKot) {
        initRvAdapter(t.list)
    }
}
