package co.touchlab.droidconandroid.ui

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.touchlab.droidconandroid.R
import co.touchlab.droidconandroid.data.TalkSubmission
import java.util.*

/**
 *
 * Created by toidiu on 8/6/15.
 */
class VoteAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    val VIEW_TYPE_VOTE: Int = 0
    val VIEW_TYPE_PAGE_TITLE: Int = 1
    val title = "Open for voting"

    private var dataSet: List<Any> = ArrayList()
    private val voteClickListener: VoteClickListener

    constructor(data: List<TalkSubmission>, eventClickListener: VoteClickListener) : super() {
//        dataSet + title + events
        dataSet += title
        dataSet += data
        this.voteClickListener = eventClickListener
    }

    override fun getItemCount(): Int {
        return dataSet.size()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        val v: View
        if (viewType == VIEW_TYPE_PAGE_TITLE) {
            v = LayoutInflater.from(parent!!.getContext()).inflate(R.layout.item_vote_title, parent, false)
            return VoteTitleViewHolder(v)
        } else if (viewType == VIEW_TYPE_VOTE) {
            v = LayoutInflater.from(parent!!.getContext()).inflate(R.layout.item_vote, parent, false)
            return VoteBlockViewHolder(v)
        }
        throw UnsupportedOperationException()
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val context = holder!!.itemView.getContext()

        if (getItemViewType(position) == VIEW_TYPE_VOTE ) {

            holder as VoteBlockViewHolder
            val talk = dataSet.get(position) as TalkSubmission

            holder.title.setText(talk.title)
            holder.descrip.setText(talk.description)

            holder.card.setOnClickListener {
                voteClickListener.onEventClick(talk)
            }


        } else if (getItemViewType(position) == VIEW_TYPE_PAGE_TITLE ) {
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = dataSet.get(position)
        if (item is TalkSubmission) {
            return VIEW_TYPE_VOTE
        } else if (item is String) {
            return VIEW_TYPE_PAGE_TITLE
        }
        throw UnsupportedOperationException()
    }

    fun remove(){
//        dataSet.
    }

    public class VoteTitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    public class VoteBlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        public val title: TextView
        public val descrip: TextView
        public val card: View

        init {
            title = itemView.findViewById(R.id.title) as TextView
            descrip = itemView.findViewById(R.id.description) as TextView
            card = itemView.findViewById(R.id.card)
        }
    }

}

interface VoteClickListener {

    fun onEventClick(item: TalkSubmission)

}
