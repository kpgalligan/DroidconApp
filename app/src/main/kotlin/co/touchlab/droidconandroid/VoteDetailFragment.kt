package co.touchlab.droidconandroid

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import co.touchlab.android.threading.tasks.TaskQueue
import co.touchlab.droidconandroid.data.TalkSubmission
import co.touchlab.droidconandroid.tasks.UpdateDbVoteTask
import co.touchlab.droidconandroid.ui.CustomRatingBar


/**
 *
 * Created by toidiu on 8/5/15.
 */
class VoteDetailFragment : DialogFragment() {

    companion object {
        val TALK = "TALK"

        fun newInstance(talk: TalkSubmission): VoteDetailFragment {
            val fragment = VoteDetailFragment()
            val args = Bundle()
            args.putSerializable(TALK, talk)
            fragment.arguments = args
            fragment.isCancelable = true
            return fragment
        }
    }

    var rating: CustomRatingBar? = null
    var talk: TalkSubmission? = null

    override fun onCreateView(inflater: LayoutInflater?, container: view.ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_vote_detail, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<DialogFragment>.onCreate(savedInstanceState)
        talk = arguments.getSerializable(TALK) as TalkSubmission

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super<DialogFragment>.onActivityCreated(savedInstanceState)

        initEvent()
    }

    fun initEvent() {
        val title = view.findViewById(R.id.title) as TextView
        val descrip = view.findViewById(R.id.description) as TextView
        val speaker = view.findViewById(R.id.speaker) as TextView

        val cancel = view.findViewById(R.id.cancel)
        val pass = view.findViewById(R.id.pass) as TextView
        val submit = view.findViewById(R.id.submit_vote) as TextView

        title.text = talk!!.title
        descrip.text = talk!!.description
        speaker.text = talk!!.speaker

        cancel.setOnClickListener { dismiss() }
        pass.setOnClickListener {
            talk!!.vote = 0
            updateTalk()
            dismiss()
        }
        submit.setOnClickListener {
            if (talk!!.vote == null) {
                Toast.makeText(activity, "Please select a rating.", Toast.LENGTH_SHORT).show()
            } else {
                updateTalk()
                dismiss()
            }
        }

        initRating()
    }

    private fun updateTalk() {
        TaskQueue.loadQueueDefault(activity).execute(UpdateDbVoteTask(talk!!))
    }

    private fun initRating() {
        rating = view.findViewById(R.id.rating) as CustomRatingBar
        if (talk!!.vote != null)
            rating!!.setRating(talk!!.vote.toFloat())

        rating!!.setChangeListener(object : CustomRatingBar.RatingChangeListener {
            override fun onChange(rate: Int) {
                talk!!.vote = rate
            }

        })

        //        = object : RatingBar.OnRatingBarChangeListener {
        //            override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
        //            }
        //
        //        }
    }
}
