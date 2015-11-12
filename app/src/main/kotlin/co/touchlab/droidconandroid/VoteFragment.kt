package co.touchlab.droidconandroid

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view
import android.view.*
import android.widget.TextView
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.android.threading.tasks.TaskQueue
import co.touchlab.droidconandroid.data.TalkSubmission
import co.touchlab.droidconandroid.tasks.GetDbTalkSubmissionTask
import co.touchlab.droidconandroid.ui.RemoveTalkListener
import co.touchlab.droidconandroid.ui.VoteAdapter
import co.touchlab.droidconandroid.ui.VoteClickListener
import java.util.*


/**
 *
 * Created by toidiu on 8/5/15.
 */
class VoteFragment : Fragment(), VoteClickListener {

    var empty: TextView? = null
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

        empty = view.findViewById(R.id.empty_list) as TextView
        rv = view.findViewById(R.id.rv) as RecyclerView
        rv!!.layoutManager = LinearLayoutManager(activity)

        (activity as AppCompatActivity).supportActionBar.setTitle(R.string.vote)
        TaskQueue.loadQueueDefault(activity).execute(GetDbTalkSubmissionTask(true))
    }

    fun initRvAdapter(data: List<TalkSubmission>, openVotes: Boolean) {
        adapter = VoteAdapter(data, this, openVotes)
        rv!!.setAdapter(adapter!!)

        refreshView()
    }

    private fun refreshView() {
        if (adapter!!.isDataEmpty()) {
            when (adapter!!.displayState()) {
                true ->
                    empty!!.setText(R.string.empty_open_vote)
                false ->
                    empty!!.setText(R.string.empty_close_vote)
            }
            empty!!.visibility = View.VISIBLE

        } else {
            empty!!.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.vote_list, menu)
        votedList = menu!!.findItem(R.id.action_voted)
        return super<Fragment>.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when {
            item!!.itemId == R.id.action_voted -> {

                votedList!!.setChecked(!votedList!!.isChecked)

                when (votedList!!.isChecked) {
                    true -> {
                        votedList!!.setIcon(R.drawable.ic_voted)
                        TaskQueue.loadQueueDefault(activity).execute(GetDbTalkSubmissionTask(true))
                    }
                    false -> {
                        votedList!!.setIcon(R.drawable.ic_notvoted)
                        TaskQueue.loadQueueDefault(activity).execute(GetDbTalkSubmissionTask(false))
                    }
                }

            }
        }
        return super<Fragment>.onOptionsItemSelected(item)
    }

    override fun onTalkItemClick(item: TalkSubmission) {
        val fragment = VoteDetailFragment.newInstance(item)
        fragment.show(activity.supportFragmentManager, null)
    }

    //----------EVENT------------------
    public fun onEventMainThread(t: GetDbTalkSubmissionTask) {
        initRvAdapter(t.list, t.openVotes)
    }

    public fun onEventMainThread(t: RemoveTalkListener) {
        adapter!!.remove(t.item)
        refreshView()
    }
}


