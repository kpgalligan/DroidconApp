package co.touchlab.droidconandroid

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view
import android.view.LayoutInflater
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.android.threading.tasks.TaskQueue
import co.touchlab.droidconandroid.data.DatabaseHelper
import co.touchlab.droidconandroid.data.Event
import co.touchlab.droidconandroid.tasks.EventDetailLoadTask


/**
 *
 * Created by toidiu on 8/5/15.
 */
class VoteDetailFragment : Fragment() {

    companion object {
        val EVENT_ID = "EVENT_ID"

        fun newInstance(id: Long): VoteDetailFragment {
            val fragment = VoteDetailFragment()
            val args = Bundle()
            args.putLong(EVENT_ID, id)
            fragment.arguments = args
            return fragment
        }
    }

    var rating: RatingBar? = null
    var eventId: Long? = null
    var event: Event? = null

    override fun onCreateView(inflater: LayoutInflater?, container: view.ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_vote_detail, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Fragment>.onCreate(savedInstanceState)
        EventBusExt.getDefault().register(this)
        eventId = arguments.getLong(EVENT_ID)

        (activity as AppCompatActivity).supportActionBar.title = ""

        TaskQueue.loadQueueDefault(activity).execute(EventDetailLoadTask(eventId!!))
    }

    override fun onDestroy() {
        super<Fragment>.onDestroy()
        EventBusExt.getDefault().unregister(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super<Fragment>.onActivityCreated(savedInstanceState)
    }

    fun initEvent() {
        val title = view.findViewById(R.id.title) as TextView
        val descrip = view.findViewById(R.id.description) as TextView
        val speaker = view.findViewById(R.id.speaker) as TextView


        title.text = "" + event!!.name
        descrip.text = event!!.description
        speaker.text = event!!.allSpeakersString()

        initRating()
    }

    private fun initRating() {
        val dao = DatabaseHelper.getInstance(activity).eventDao
        val handler = Handler(activity.mainLooper);

        rating = view.findViewById(R.id.rating) as RatingBar
        rating!!.rating = event!!.vote.toFloat()

        rating!!.onRatingBarChangeListener = object : RatingBar.OnRatingBarChangeListener {
            override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {

                event!!.vote = Math.round(rating)

                handler.removeCallbacksAndMessages(null);
                val eventRunnable = Runnable() {
                    run { dao.update(Event()) }
                };
                handler.postDelayed(eventRunnable, 1000L)
            }

        }
    }


    //----------EVENT------------------
    public fun onEventMainThread(t: EventDetailLoadTask) {
        this.event = t.event!!
        initEvent()
    }
}
