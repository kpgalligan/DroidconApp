package co.touchlab.droidconandroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.text.format.DateUtils
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.droidconandroid.data.AppPrefs
import co.touchlab.droidconandroid.data.DatabaseHelper
import co.touchlab.droidconandroid.data.Track
import co.touchlab.droidconandroid.gcm.RegistrationIntentService
import co.touchlab.droidconandroid.presenter.AppManager
import co.touchlab.droidconandroid.presenter.ConferenceDataHost
import co.touchlab.droidconandroid.presenter.ConferenceDataPresenter
import co.touchlab.droidconandroid.presenter.ConferenceDayHolder
import co.touchlab.droidconandroid.superbus.UploadAvatarCommand
import co.touchlab.droidconandroid.superbus.UploadCoverCommand
import co.touchlab.droidconandroid.tasks.persisted.RefreshScheduleData
import co.touchlab.droidconandroid.ui.*
import co.touchlab.droidconandroid.utils.TimeUtils
import com.wnafee.vector.compat.ResourcesCompat
import kotlinx.android.synthetic.main.activity_schedule.*
import kotlinx.android.synthetic.main.include_schedule_viewpager.*
import java.text.SimpleDateFormat
import java.util.*

private const val POSITION_EXPLORE = 1
private const val POSITION_MY_SCHEDULE = 2
private const val SELECTED_TRACKS = "tracks"
private const val ALL_EVENTS = "all_events"

class ScheduleActivity : AppCompatActivity(), FilterInterface, NfcAdapter.CreateNdefMessageCallback
{
    private var conferenceDataPresenter: ConferenceDataPresenter? = null
    private var drawerAdapter: DrawerAdapter? = null
    private var filterAdapter: FilterAdapter? = null
    private var pagerAdapter: ScheduleFragmentPagerAdapter? = null

    private var allEvents = true

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

        when (AppManager.findStartScreen(getString(R.string.voting_ends)))
        {
            AppManager.AppScreens.Welcome ->
            {
                startActivity(WelcomeActivity.getLaunchIntent(this@ScheduleActivity, false))
                finish()
                return
            }
            AppManager.AppScreens.Login ->
            {
                startActivity(SignInActivity.getLaunchIntent(this@ScheduleActivity))
                finish()
                return
            }
            AppManager.AppScreens.Voting ->
            {
                VotingIntroActivity.callMe(this@ScheduleActivity)
                finish()
                return
            }
        }

        setContentView(R.layout.activity_schedule)

        setupToolbar()
        setupNavigationDrawer()
        setupFilterDrawer()
        initNfc()

        if (savedInstanceState != null)
        {
            val filters = savedInstanceState.getStringArrayList(SELECTED_TRACKS)
            val tracks = ArrayList<Track>()
            for (trackServerName in filters)
            {
                tracks.add(Track.findByServerName(trackServerName))
            }
            allEvents = savedInstanceState.getBoolean(ALL_EVENTS)
            filterAdapter !!.setSelectedTracks(tracks)
        }
        adjustToolBarAndDrawers()

        EventBusExt.getDefault().register(this)

        // Start IntentService to register this application with GCM.
        val intent = Intent(this, RegistrationIntentService::class.java)
        startService(intent)
    }

    override fun onResume()
    {
        super.onResume()

        Handler().post(RefreshRunnable())

        val prefs = AppPrefs.getInstance(this)
        val lastRefresh = prefs.getRefreshTime()

        if (prefs.isLoggedIn()
                && (System.currentTimeMillis() - lastRefresh > (DateUtils.HOUR_IN_MILLIS * 6)))
        {
            RefreshScheduleData.callMe(this)
        }
    }

    fun onEventMainThread(eventDetailTask: RefreshScheduleData)
    {
        Handler().post(RefreshRunnable())
    }

    fun onEventMainThread(command: UploadAvatarCommand)
    {
        drawerAdapter !!.notifyDataSetChanged()
    }

    fun onEventMainThread(command: UploadCoverCommand)
    {
        drawerAdapter !!.notifyDataSetChanged()
    }

    override fun onBackPressed()
    {
        when {
            drawer_layout.isDrawerOpen(filter_wrapper) -> drawer_layout.closeDrawer(filter_wrapper)
            drawer_layout.isDrawerOpen(drawer_recycler) -> drawer_layout.closeDrawer(drawer_recycler)
            else -> super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(SELECTED_TRACKS, getCurrentFilters())
        outState.putBoolean(ALL_EVENTS, allEvents);
    }

    override fun onDestroy()
    {
        super.onDestroy()
        conferenceDataPresenter?.unregister()
        EventBusExt.getDefault().unregister(this)
    }

    private fun setupToolbar()
    {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        schedule_backdrop.setImageDrawable(ResourcesCompat.getDrawable(this, R.drawable.superglyph_outline360x114dp))
        appbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val percentage : Float = 1 - (Math.abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange)
            schedule_toolbar_title?.alpha = percentage;
        };
    }

    private fun setupNavigationDrawer()
    {
        var drawerToggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawer_layout.setDrawerListener(drawerToggle)

        drawerToggle.syncState();

        drawerAdapter = DrawerAdapter(getDrawerItems(), object : DrawerClickListener
        {
            override fun onNavigationItemClick(position: Int, titleRes: Int)
            {
                drawer_layout.closeDrawer(drawer_recycler)

                when (titleRes)
                {
                    R.string.explore ->
                    {
                        allEvents = true;
                        appbar.setExpanded(true)
                    }
                    R.string.my_schedule ->
                    {
                        allEvents = false;
                        appbar.setExpanded(true)
                    }
                    R.string.buy_tickets ->
                    {
                        var i = Intent(Intent.ACTION_VIEW);
                        i.data = Uri.parse(getString(R.string.buy_ticket_url));
                        startActivity(i);
                    }

                    R.string.social -> FindUserKot.startMe(this@ScheduleActivity)
                    R.string.profile -> EditUserProfile.callMe(this@ScheduleActivity)
                    R.string.sponsors -> startActivity(WelcomeActivity.getLaunchIntent(this@ScheduleActivity,
                            true))
                    R.string.about -> AboutActivity.callMe(this@ScheduleActivity)
                }

                Handler().post(RefreshRunnable())
                drawerAdapter !!.setSelectedPosition(position)
                adjustToolBarAndDrawers()
                filterAdapter !!.clearSelectedTracks()
            }

            override fun onHeaderItemClick()
            {
                val userId = AppPrefs.getInstance(this@ScheduleActivity).getUserId()
                if (userId != null)
                {
                    val ua = DatabaseHelper.getInstance(this@ScheduleActivity).getUserAccountDao().queryForId(
                            userId)
                    if (ua != null && ua.userCode != null && ! TextUtils.isEmpty(ua.userCode))
                    {
                        drawer_layout.closeDrawer(drawer_recycler)
                        UserDetailActivity.callMe(this@ScheduleActivity, ua.userCode)
                    }
                }
            }
        })
        drawer_recycler.adapter = drawerAdapter
        drawer_recycler.layoutManager = LinearLayoutManager(this)
    }

    private fun getDrawerItems(): List<Any>
    {

        var drawerItems = ArrayList<Any>()
        drawerItems.add("header_placeholder")
        drawerItems.add(NavigationItem(R.string.explore, R.drawable.ic_explore))
        drawerItems.add(NavigationItem(R.string.my_schedule, R.drawable.ic_myschedule))
        drawerItems.add(NavigationItem(R.string.buy_tickets, R.drawable.ic_action_ticket))
        drawerItems.add("divider_placeholder")
        drawerItems.add(NavigationItem(R.string.profile, R.drawable.ic_settings))
        drawerItems.add(NavigationItem(R.string.about, R.drawable.ic_info))
        return drawerItems;
    }

    private fun setupFilterDrawer()
    {
        filter_recycler.layoutManager = LinearLayoutManager(this)
        filterAdapter = FilterAdapter(getFilterItems(), object : FilterClickListener
        {
            override fun onFilterClick(track: Track)
            {
                pagerAdapter !!.updateFrags(track)
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

    private fun adjustToolBarAndDrawers()
    {
        //TODO toggle theme/colors
        if (allEvents)
        {
            schedule_toolbar_title.setText(R.string.app_name)
            drawerAdapter !!.setSelectedPosition(POSITION_EXPLORE)
        }
        else
        {
            schedule_toolbar_title.setText(R.string.my_schedule)
            drawerAdapter !!.setSelectedPosition(POSITION_MY_SCHEDULE)
        }
    }

// TODO determine if we're just changing how filter drawer is accessed or disabling it
// Uncomment to reimplement filter and search UI

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean
//    {
//        menuInflater.inflate(R.menu.home, menu)
//        val filter = menu !!.findItem(R.id.action_filter)
//        filter.icon = ResourcesCompat.getDrawable(this, R.drawable.ic_filter)
//        val search = menu.findItem(R.id.action_search)
//        search.icon = ResourcesCompat.getDrawable(this, R.drawable.ic_search)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean
//    {
//        when
//        {
//            item !!.itemId == R.id.action_filter ->
//            {
//                drawerLayout.openDrawer(findViewById(R.id.filter_wrapper))
//            }
//            item.itemId == R.id.action_search ->
//            {
//                FindUserKot.startMe(this@ScheduleActivity)
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

    private fun initNfc()
    {
        var nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null)
        {
            nfcAdapter.setNdefPushMessageCallback(this, this)
        }
    }

    override fun createNdefMessage(event: NfcEvent?): NdefMessage?
    {
        val userId = AppPrefs.getInstance(this@ScheduleActivity).getUserId()
        if (userId != null)
        {
            val ua = DatabaseHelper.getInstance(this@ScheduleActivity).getUserAccountDao().queryForId(
                    userId)
            if (ua != null && ua.userCode != null && ! TextUtils.isEmpty(ua.userCode))
            {
                var msg = NdefMessage(arrayOf(NdefRecord.createMime("application/vnd.co.touchlab.droidconandroid",
                        ua.userCode.toByteArray())
                        , NdefRecord.createApplicationRecord("co.touchlab.droidconandroid")))
                return msg
            }
        }
        return null;
    }

    class ConfHost : ConferenceDataHost
    {
        override fun loadCallback(conferenceDayHolders: Array<out ConferenceDayHolder>?)
        {
            EventBusExt.getDefault() !!.post(conferenceDayHolders)
        }
    }

    inner class RefreshRunnable() : Runnable
    {
        override fun run()
        {
            conferenceDataPresenter?.unregister()
            conferenceDataPresenter = ConferenceDataPresenter(this@ScheduleActivity,
                    ConfHost(),
                    allEvents)

            val dates: ArrayList<Long> = ArrayList<Long>()
            val startString: String? = AppPrefs.getInstance(this@ScheduleActivity).getConventionStartDate()
            val endString: String? = AppPrefs.getInstance(this@ScheduleActivity).getConventionEndDate()

            if (! TextUtils.isEmpty(startString) && ! TextUtils.isEmpty(endString))
            {
                var start: Long = TimeUtils.sanitize(TimeUtils.DATE_FORMAT.get().parse(startString))
                val end: Long = TimeUtils.sanitize(TimeUtils.DATE_FORMAT.get().parse(endString))

                while (start <= end)
                {
                    dates.add(start)
                    start += DateUtils.DAY_IN_MILLIS
                }

                pagerAdapter = ScheduleFragmentPagerAdapter(
                        supportFragmentManager,
                        dates,
                        allEvents)
                view_pager.adapter = pagerAdapter;
                view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
                tabs.setupWithViewPager(view_pager)
            }
        }
    }

    class ScheduleFragmentPagerAdapter : FragmentPagerAdapter
    {
        private var dates: List<Long>
        private var allEvents: Boolean
        private var fragmentManager: FragmentManager

        private val tabDateFormat = SimpleDateFormat("MMM dd")

        constructor(fm: FragmentManager, dates: List<Long>, allEvents: Boolean) : super(fm)
        {
            this.dates = dates;
            this.allEvents = allEvents
            this.fragmentManager = fm
        }

        override fun getCount(): Int
        {
            return dates.size
        }

        override fun getItem(position: Int): ScheduleDataFragment?
        {
            return ScheduleDataFragment.newInstance(allEvents, dates.get(position), position)
        }

        override fun getPageTitle(position: Int): CharSequence?
        {
            return tabDateFormat.format(Date(dates.get(position)))
        }

        fun updateFrags(track: Track)
        {

            for (fragment in fragmentManager.getFragments())
            {
                if (fragment != null)
                {
                    (fragment as ScheduleDataFragment).filter(track)
                }
            }

        }
    }
}

interface FilterInterface
{
    fun getCurrentFilters(): ArrayList<String>
}
