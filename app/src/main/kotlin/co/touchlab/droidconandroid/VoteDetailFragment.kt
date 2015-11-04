package co.touchlab.droidconandroid

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view
import android.view.LayoutInflater
import android.view.View


/**
 *
 * Created by toidiu on 8/5/15.
 */
class VoteDetailFragment : Fragment() {

    //    var test: TextView? = null
    //    var rv: RecyclerView? = null
    //    var adapter: VoteAdapter? = null

    companion object {
        val EVENT_ID = "EVENT_ID"

        fun newInstance(id: Long): VoteDetailFragment {
            val fragment = VoteDetailFragment()
            val args = Bundle()
            args.putLong(EVENT_ID, id)
            fragment.setArguments(args)
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: view.ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_vote, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Fragment>.onCreate(savedInstanceState)
        //        EventBusExt.getDefault().register(this)
    }

    override fun onDestroy() {
        super<Fragment>.onDestroy()
        //        EventBusExt.getDefault().unregister(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super<Fragment>.onActivityCreated(savedInstanceState)

    }


    //----------EVENT------------------
    //    public fun onEventMainThread(t: FindVoteTaskKot) {
    //    }
}
