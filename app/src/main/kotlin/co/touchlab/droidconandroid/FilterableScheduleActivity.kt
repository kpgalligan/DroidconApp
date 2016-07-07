package co.touchlab.droidconandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import co.touchlab.droidconandroid.data.Track
import co.touchlab.droidconandroid.ui.FilterAdapter
import co.touchlab.droidconandroid.ui.FilterClickListener
import kotlinx.android.synthetic.main.activity_schedule.*
import kotlinx.android.synthetic.main.include_schedule_viewpager.*
import java.util.*

/**
 * Created by Ramona Harrison
 * on 7/6/16.
 *
 * An extension of ScheduleActivity with a right-side Filter drawer. If you want filterable schedules,
 * replace all usages of ScheduleActivity with this.
 */

private const val SELECTED_TRACKS = "tracks"

class FilterableScheduleActivity : ScheduleActivity(), FilterInterface
{

    private var filterAdapter: FilterAdapter? = null

    companion object
    {
        fun startMe(c: Context)
        {
            val i = Intent(c, ScheduleActivity::class.java)
            c.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, filter_wrapper);
        setupFilterDrawer()

        if (savedInstanceState != null)
        {
            val filters = savedInstanceState.getStringArrayList(SELECTED_TRACKS)
            val tracks = ArrayList<Track>()
            for (trackServerName in filters)
            {
                tracks.add(Track.findByServerName(trackServerName))
            }
            filterAdapter !!.setSelectedTracks(tracks)
        }
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(SELECTED_TRACKS, getCurrentFilters())
    }


    private fun setupFilterDrawer()
    {
        filter_wrapper.visibility = View.VISIBLE
        schedule_toolbar_profile.visibility = View.GONE
        filter_recycler.layoutManager = LinearLayoutManager(this)
        filterAdapter = FilterAdapter(getFilterItems(), object : FilterClickListener
        {
            override fun onFilterClick(track: Track)
            {
                (view_pager.adapter as ScheduleFragmentPagerAdapter).updateFrags(track)
            }
        })
        filter_recycler.adapter = filterAdapter

        back.setOnClickListener {
            drawer_layout.closeDrawer(filter_wrapper)
        }
    }

    private fun getFilterItems(): List<Any>
    {
        var filterItems = ArrayList<Any>()
        filterItems.add(getString(R.string.tracks))
        filterItems.add(Track.DEVELOPMENT)
        filterItems.add(Track.DESIGN)
        filterItems.add(Track.BUSINESS)
        return filterItems
    }

    override fun getCurrentFilters(): ArrayList<String>
    {
        val filters = ArrayList<String>()
        for (track in filterAdapter !!.getSelectedTracks())
        {
            filters.add(track.getServerName())
        }
        return filters
    }

    override fun onBackPressed()
    {
        when {
            drawer_layout.isDrawerOpen(filter_wrapper) -> drawer_layout.closeDrawer(filter_wrapper)
            else -> super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.home, menu)
        val filter = menu !!.findItem(R.id.action_filter)
        filter.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_filter, null)
        val search = menu.findItem(R.id.action_search)
        search.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_search, null)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean
    {
        when
        {
            item !!.itemId == R.id.action_filter ->
            {
                drawer_layout.openDrawer(findViewById(R.id.filter_wrapper))
            }
            item.itemId == R.id.action_search ->
            {
                FindUserKot.startMe(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

interface FilterInterface
{
    fun getCurrentFilters(): ArrayList<String>
}
