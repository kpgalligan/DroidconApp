package co.touchlab.droidconandroid.ui

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
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
    val openTitle = "Open for voting"
    val closeTitle = "Votes submitted"
    var openVotes: Boolean? = false

    private var dataSet: MutableList<Any> = ArrayList()
    private val voteClickListener: VoteClickListener

    constructor(data: List<TalkSubmission>, eventClickListener: VoteClickListener, openVotes: Boolean) : super() {
        when (openVotes) {
            true ->
                dataSet.add(openTitle)

            false ->
                dataSet.add(closeTitle)
        }

        dataSet.addAll(data)
        this.openVotes = openVotes;
        this.voteClickListener = eventClickListener
    }

    override fun getItemCount(): Int {
        return dataSet.size
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
        val resources = holder!!.itemView.context.resources

        if (getItemViewType(position) == VIEW_TYPE_VOTE ) {

            holder as VoteBlockViewHolder
            val talk = dataSet.elementAt(position) as TalkSubmission

            holder.title.setText(talk.title)
            holder.descrip.setText(talk.description)
            when (openVotes) {
                true ->
                    holder.card.setCardBackgroundColor(resources.getColor(R.color.white))
                false ->
                    holder.card.setCardBackgroundColor(resources.getColor(R.color.vote_card_gray))
            }

            holder.card.setOnClickListener {
                voteClickListener.onTalkItemClick(talk)
            }


        } else if (getItemViewType(position) == VIEW_TYPE_PAGE_TITLE ) {
            holder as VoteTitleViewHolder
            holder.pageTitle.text = dataSet.elementAt(position).toString()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = dataSet.elementAt(position)
        if (item is TalkSubmission) {
            return VIEW_TYPE_VOTE
        } else if (item is String) {
            return VIEW_TYPE_PAGE_TITLE
        }
        throw UnsupportedOperationException()
    }

    fun isDataEmpty(): Boolean {
        return dataSet.size <= 1
    }

    fun remove(item: TalkSubmission) {
        if (!openVotes!!) return

        val iterator = dataSet.iterator()
        var i = 0

        while (iterator.hasNext()) {
            val d = iterator.next()
            if (d is TalkSubmission && d.id == item.id) {
                dataSet.remove(i)
                notifyItemRemoved(i)
                break
            }
            i++
        }

    }

    fun displayState(): Boolean {
        return this.openVotes!!
    }

}

//-------------------------VIEW HOLDER
public class VoteTitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    public val pageTitle: TextView

    init {
        pageTitle = itemView.findViewById(R.id.page_title) as TextView
    }
}

public class VoteBlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    public val title: TextView
    public val descrip: TextView
    public val card: CardView

    init {
        title = itemView.findViewById(R.id.title) as TextView
        descrip = itemView.findViewById(R.id.description) as TextView
        card = itemView.findViewById(R.id.card) as CardView
    }
}

//-------------------------INTEFACE
interface VoteClickListener {

    fun onTalkItemClick(item: TalkSubmission)

}
