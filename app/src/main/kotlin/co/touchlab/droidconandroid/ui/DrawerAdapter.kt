package co.touchlab.droidconandroid.ui

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.touchlab.droidconandroid.HTTPS_S3_AMAZONAWS_COM_DROIDCONIMAGES
import co.touchlab.droidconandroid.R
import co.touchlab.droidconandroid.data.AppPrefs
import com.squareup.picasso.Picasso
import com.wnafee.vector.compat.ResourcesCompat
import kotlinx.android.synthetic.main.item_drawer.view.*
import kotlinx.android.synthetic.main.item_drawer_header.view.*

private const val VIEW_TYPE_HEADER: Int = 0
private const val VIEW_TYPE_NAVIGATION: Int = 1
private const val VIEW_TYPE_DIVIDER: Int = 2

class DrawerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    var selectedPos: Int = 1

    private val dataSet: List<Any>
    private val drawerClickListener: DrawerClickListener

    constructor(drawerItems: List<Any>, drawerClickListener: DrawerClickListener) : super()
    {
        dataSet = drawerItems
        this.drawerClickListener = drawerClickListener
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int)
    {
        val context = holder !!.itemView.context
        if (getItemViewType(position) == VIEW_TYPE_HEADER)
        {
            val headerHolder = holder as HeaderViewHolder
            val avatarKey = AppPrefs.getInstance(context).avatarKey
            if (! TextUtils.isEmpty(avatarKey))
            {

                Picasso.with(context)
                        .load(HTTPS_S3_AMAZONAWS_COM_DROIDCONIMAGES + avatarKey)
                        .into(headerHolder.itemView.avatar)

            }

            val coverKey = AppPrefs.getInstance(context).coverKey
            if (! TextUtils.isEmpty(coverKey))
            {
                Picasso.with(context)

                        .load(HTTPS_S3_AMAZONAWS_COM_DROIDCONIMAGES + coverKey)
                        .into(headerHolder.itemView.cover)
            }

            headerHolder.itemView.name.text = AppPrefs.getInstance(context).name
            headerHolder.itemView.email.text = AppPrefs.getInstance(context).email

            headerHolder.itemView.setOnClickListener {
                if (selectedPos != position)
                {
                    drawerClickListener.onHeaderItemClick()
                }
            }

        }
        else if (getItemViewType(position) == VIEW_TYPE_NAVIGATION)
        {
            val navItem = dataSet[position] as NavigationItem
            val navHolder = holder as NavigationViewHolder
            navHolder.itemView.title.setText(navItem.titleRes)
            val drawable = ResourcesCompat.getDrawable(context, navItem.iconRes)

            val selected = selectedPos == position
            navHolder.itemView.isSelected = selected
            if (selected)
            {
                drawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context,
                        R.color.nav_icon_highlight), PorterDuff.Mode.SRC_IN)
                navHolder.itemView.highlight.visibility = View.VISIBLE
            }
            else
            {
                drawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context,
                        R.color.drawer_icons), PorterDuff.Mode.SRC_IN)
                navHolder.itemView.highlight.visibility = View.GONE
            }
            navHolder.itemView.icon.setImageDrawable(drawable)

            navHolder.itemView.setOnClickListener {
                if (selectedPos != position)
                {
                    drawerClickListener.onNavigationItemClick(position, navItem.titleRes)
                }
            }
        }


    }

    fun setSelectedPosition(position: Int)
    {
        selectedPos = position
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int
    {
        return dataSet.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder?
    {
        val v: View
        if (viewType == VIEW_TYPE_NAVIGATION)
        {
            v = LayoutInflater.from(parent !!.context).inflate(R.layout.item_drawer, parent, false)
            return NavigationViewHolder(v)
        }
        else if (viewType == VIEW_TYPE_HEADER)
        {
            v = LayoutInflater.from(parent !!.context).inflate(R.layout.item_drawer_header,
                    parent,
                    false)
            return HeaderViewHolder(v)
        }
        else
        {
            v = LayoutInflater.from(parent !!.context).inflate(R.layout.item_drawer_divider,
                    parent,
                    false)
            return DividerViewHolder(v)
        }

    }

    override fun getItemViewType(position: Int): Int
    {
        if (position == 0)
        {
            return VIEW_TYPE_HEADER
        }
        else if (dataSet[position] is NavigationItem)
        {
            return VIEW_TYPE_NAVIGATION
        }
        else
        {
            return VIEW_TYPE_DIVIDER
        }
    }

    class NavigationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {}

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {}

    class DividerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {}
}

interface DrawerClickListener
{

    fun onNavigationItemClick(position: Int, titleRes: Int)

    fun onHeaderItemClick()

}

class NavigationItem
{

    val titleRes: Int
    val iconRes: Int

    constructor(titleRes: Int, iconRes: Int)
    {
        this.titleRes = titleRes
        this.iconRes = iconRes
    }
}
