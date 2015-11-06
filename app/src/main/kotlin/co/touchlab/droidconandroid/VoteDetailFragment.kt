package co.touchlab.droidconandroid

import android.os.Bundle
import android.support.v4.app.DialogFragment
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
class VoteDetailFragment : DialogFragment() {

    companion object {
        val EVENT_ID = "EVENT_ID"

        fun newInstance(id: Long): VoteDetailFragment {
            val fragment = VoteDetailFragment()
            val args = Bundle()
            args.putLong(EVENT_ID, id)
            fragment.arguments = args
            fragment.isCancelable = true
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
        super<DialogFragment>.onCreate(savedInstanceState)
        EventBusExt.getDefault().register(this)
        eventId = arguments.getLong(EVENT_ID)

        TaskQueue.loadQueueDefault(activity).execute(EventDetailLoadTask(eventId!!))
    }

    override fun onDestroy() {
        super<DialogFragment>.onDestroy()
        EventBusExt.getDefault().unregister(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super<DialogFragment>.onActivityCreated(savedInstanceState)
    }

    fun initEvent() {
        val title = view.findViewById(R.id.title) as TextView
        val descrip = view.findViewById(R.id.description) as TextView
        val speaker = view.findViewById(R.id.speaker) as TextView


        val cancel = view.findViewById(R.id.cancel)
        val pass = view.findViewById(R.id.pass) as TextView
        val submit = view.findViewById(R.id.submit_vote) as TextView

        val dao = DatabaseHelper.getInstance(activity).eventDao

        title.text = "" + event!!.name
        descrip.text = event!!.description
        speaker.text = event!!.allSpeakersString()

        cancel.setOnClickListener { fragmentManager.popBackStackImmediate() }
        pass.setOnClickListener {
            event!!.vote = 0
            dao.update(event)
            fragmentManager.popBackStackImmediate()
        }
        submit.setOnClickListener {
            dao.update(event)
            fragmentManager.popBackStackImmediate()
        }

        initRating()
    }

    private fun initRating() {

        rating = view.findViewById(R.id.rating) as RatingBar
        rating!!.rating = event!!.vote.toFloat()

        rating!!.onRatingBarChangeListener = object : RatingBar.OnRatingBarChangeListener {
            override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
                event!!.vote = Math.round(rating)
            }

        }
    }

    //----------EVENT------------------
    public fun onEventMainThread(t: EventDetailLoadTask) {
        this.event = t.event!!
        initEvent()
    }
}
