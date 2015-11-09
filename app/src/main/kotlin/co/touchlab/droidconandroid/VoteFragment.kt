package co.touchlab.droidconandroid

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view
import android.view.*
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.android.threading.tasks.TaskQueue
import co.touchlab.droidconandroid.data.Event
import co.touchlab.droidconandroid.tasks.FindVoteDbTaskKot
import co.touchlab.droidconandroid.tasks.GetTalkSubmissionTask
import co.touchlab.droidconandroid.ui.VoteAdapter
import co.touchlab.droidconandroid.ui.VoteClickListener


/**
 *
 * Created by toidiu on 8/5/15.
 */
class VoteFragment : Fragment() ,VotedListener {

    var rv: RecyclerView? = null
    var adapter: VoteAdapter? = null
    var votedList: MenuItem? = null

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
        rv!!.layoutManager = LinearLayoutManager(activity)

        (activity as AppCompatActivity).supportActionBar.setTitle(R.string.vote)

        //FIXME change to get talkSubmissions rather than event
        TaskQueue.loadQueueDefault(activity).execute(FindVoteDbTaskKot(true))
    }

    fun initRvAdapter(data: List<Event>) {
        adapter = VoteAdapter(data, object : VoteClickListener {
            override fun onEventClick(event: Event) {
                val fragment = VoteDetailFragment.newInstance(event.id)
                fragment.show(activity.supportFragmentManager, null)
            }
        })

        rv!!.setAdapter(adapter!!)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.vote_list, menu)
        votedList = menu!!.findItem(R.id.action_voted)
        return super<Fragment>.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when {
            item!!.itemId == R.id.action_voted -> {

//                votedList!!.setChecked(!votedList!!.isChecked)
//
//                when (votedList!!.isChecked) {
//                    true -> {
//                        votedList!!.setIcon(R.drawable.ic_voted)
//                        TaskQueue.loadQueueDefault(activity).execute(FindVoteDbTaskKot(true))
//                    }
//                    false -> {
//                        votedList!!.setIcon(R.drawable.ic_notvoted)
//                        TaskQueue.loadQueueDefault(activity).execute(FindVoteDbTaskKot(false))
//                    }
//                }

                TaskQueue.loadQueueDefault(activity).execute(GetTalkSubmissionTask())


            }
        }
        return super<Fragment>.onOptionsItemSelected(item)
    }

    override fun eventVoted() {
        adapter!!.remove()
    }

    //----------EVENT------------------
    public fun onEventMainThread(t: FindVoteDbTaskKot) {
        initRvAdapter(t.list)
    }
}

interface VotedListener {
    fun eventVoted()
}
