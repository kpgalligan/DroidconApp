package co.touchlab.droidconandroid

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.android.threading.tasks.TaskQueue
import co.touchlab.droidconandroid.data.Event
import co.touchlab.droidconandroid.network.dao.Convention
import co.touchlab.droidconandroid.tasks.FindVoteTaskKot
import co.touchlab.droidconandroid.ui.EventAdapter
import co.touchlab.droidconandroid.ui.EventClickListener
import co.touchlab.droidconandroid.ui.VoteAdapter
import co.touchlab.droidconandroid.ui.VoteClickListener
import java.text.SimpleDateFormat


/**
 *
 * Created by toidiu on 8/5/15.
 */
class VoteFragment : Fragment() {

    var test: TextView? = null
    var rv: RecyclerView? = null
    var adapter: VoteAdapter? = null

    companion object {
        val VOTING: String = "VOTING"

        fun newInstance(): VoteFragment {
            val fragment = VoteFragment()
            val args = Bundle()
            fragment.setArguments(args)
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: view.ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_vote, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Fragment>.onCreate(savedInstanceState)
        EventBusExt.getDefault().register(this)
    }

    override fun onDestroy() {
        super<Fragment>.onDestroy()
        EventBusExt.getDefault().unregister(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super<Fragment>.onActivityCreated(savedInstanceState)

        rv = view.findViewById(R.id.rv) as RecyclerView
        rv!!.setLayoutManager(LinearLayoutManager(getActivity()))

        TaskQueue.loadQueueDefault(activity).execute(FindVoteTaskKot())
    }

    fun initRvAdapter (data: List<Event>) {
        adapter = VoteAdapter(data, (activity as FilterInterface).getCurrentFilters(), object : VoteClickListener {
            override fun onEventClick(event: Event) {
                Toast.makeText(activity, ""+event.id, Toast.LENGTH_SHORT).show()
                val fragment = VoteDetailFragment.newInstance(event.id)

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.container, fragment)
                        .commit()
            }
        })
        rv!!.setAdapter(adapter!!)
    }

    //----------EVENT------------------
    public fun onEventMainThread(t: FindVoteTaskKot) {
        initRvAdapter(t.list)
    }
}
