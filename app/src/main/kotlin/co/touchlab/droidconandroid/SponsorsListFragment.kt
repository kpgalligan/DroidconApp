package co.touchlab.droidconandroid

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_sponsors_list.*
import java.util.*

const val SPONSOR_GENERAL = 0
const val SPONSOR_STREAM = 1
const val SPONSOR_PARTY = 2
const val SPONSOR_TYPE = "SPONSOR_TYPE"
const val SPONSOR_COUNT = 3;


fun createSponsorsListFragment(type: Int): SponsorsListFragment {
    val fragment = SponsorsListFragment()
    val args = Bundle()
    args.putInt(SPONSOR_TYPE, type)
    fragment.arguments = args
    return fragment
}

class SponsorsListFragment() : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_sponsors_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var adapter = SponsorsAdapter()
        adapter.add("S-1-A", "www.google.com", 6)
        adapter.add("S-2-A", "www.google.com", 6)
        adapter.add("S-3-B", "www.google.com", 4)
        adapter.add("S-4-B", "www.google.com", 4)
        adapter.add("S-5-B", "www.google.com", 4)
        adapter.add("S-6-C", "www.google.com", 3)
        adapter.add("S-7-C", "www.google.com", 3)
        adapter.add("S-8-C", "www.google.com", 3)
        adapter.add("S-9-C", "www.google.com", 3)
        sponsor_list!!.adapter = adapter

        // Set Layout manager w/ a non-default span-size lookup
        var layoutManager = GridLayoutManager(activity, 12)
        layoutManager.spanSizeLookup = object :GridLayoutManager.SpanSizeLookup() {

            override fun getSpanSize(position: Int): Int {
                return adapter.getItemSpanSize(position)
            }

            override fun getSpanIndex(position: Int, spanCount: Int): Int {
                return position % spanCount
            }
        }
        sponsor_list!!.layoutManager = layoutManager;
    }

    inner class SponsorsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var dataset = ArrayList<SponsorItem>()

        fun add(text: String, logoRes: String, bodyRes: Int) {
            dataset.add(SponsorItem(text, logoRes, bodyRes))
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
            var view = LayoutInflater.from(parent?.context).inflate(R.layout.item_sponsor, parent, false)
            return SponsorVH(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            var vh = holder as SponsorVH
            var data = dataset.get(position)
            // vh.image!!.setText(data.imageRes)
            vh.text!!.text = data.text
            vh.itemView.setOnClickListener { Log.i("SponsorsAdapter", "Click " + position) }
        }

        override fun getItemCount(): Int {
            return dataset.size
        }

        inner class SponsorVH(val item: View) : RecyclerView.ViewHolder(item) {
            var image: ImageView? = null

            @Deprecated("") //TODO Remove, Debug only
            var text: TextView? = null

            init {
                image = item.findViewById(R.id.item_sponsor_image) as ImageView
                text = item.findViewById(R.id.debug_item_sponsor_title) as TextView
            }
        }

        inner class SponsorItem(val text: String, val image: String, val span: Int)

        fun getItemSpanSize(position: Int): Int {
            return dataset[position].span
        }
    }

}