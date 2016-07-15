package co.touchlab.droidconandroid

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.View
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
import co.touchlab.droidconandroid.tasks.Queues
import co.touchlab.droidconandroid.tasks.UpdateAlertsTask
import co.touchlab.droidconandroid.tasks.persisted.RefreshScheduleData
import co.touchlab.droidconandroid.ui.DrawerAdapter
import co.touchlab.droidconandroid.ui.DrawerClickListener
import co.touchlab.droidconandroid.ui.NavigationItem
import co.touchlab.droidconandroid.ui.UpdateAllowNotificationEvent
import co.touchlab.droidconandroid.utils.EmojiUtil
import co.touchlab.droidconandroid.utils.TimeUtils
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.wnafee.vector.compat.ResourcesCompat
import kotlinx.android.synthetic.main.activity_schedule.*
import kotlinx.android.synthetic.main.include_schedule_viewpager.*
import java.text.SimpleDateFormat
import java.util.*

const val HTTPS_S3_AMAZONAWS_COM_DROIDCONIMAGES: String = "https://s3.amazonaws.com/droidconimages/"
private const val POSITION_EXPLORE = 1
private const val POSITION_MY_SCHEDULE = 2
private const val ALL_EVENTS = "all_events"
private const val ALPHA_OPAQUE = 255

fun startScheduleActivity(c: Context) {
    c.startActivity(Intent(c, ScheduleActivity::class.java))
}

open class ScheduleActivity : AppCompatActivity(), NfcAdapter.CreateNdefMessageCallback {
    private var conferenceDataPresenter: ConferenceDataPresenter? = null
    private var allEvents = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (AppManager.findStartScreen(getString(R.string.voting_ends))) {
            AppManager.AppScreens.Welcome -> {
                startActivity(WelcomeActivity.getLaunchIntent(this@ScheduleActivity, false))
                finish()
                return
            }
            AppManager.AppScreens.Login -> {
                startActivity(SignInActivity.getLaunchIntent(this@ScheduleActivity))
                finish()
                return
            }
            AppManager.AppScreens.Voting -> {
                VotingIntroActivity.callMe(this@ScheduleActivity)
                finish()
                return
            }
            else -> {
                if (savedInstanceState != null) {
                    allEvents = savedInstanceState.getBoolean(ALL_EVENTS)
                }

                setContentView(R.layout.activity_schedule)
                setupToolbar()
                setupNavigationDrawer()
                initNfc()
                adjustToolBarAndDrawers()

                EventBusExt.getDefault().register(this)

                // Start IntentService to register this application with GCM.
                val intent = Intent(this, RegistrationIntentService::class.java)
                startService(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Handler().post(RefreshRunnable())

        val prefs = AppPrefs.getInstance(this)
        val lastRefresh = prefs.refreshTime

        if (prefs.isLoggedIn
                && (System.currentTimeMillis() - lastRefresh > (DateUtils.HOUR_IN_MILLIS * 6))) {
            RefreshScheduleData.callMe(this)
        }
    }

    override fun onBackPressed()
    {
        when {
            drawer_layout.isDrawerOpen(drawer_recycler) -> drawer_layout.closeDrawer(drawer_recycler)
            else -> super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ALL_EVENTS, allEvents)
    }

    override fun onDestroy() {
        super.onDestroy()
        conferenceDataPresenter?.unregister()
        EventBusExt.getDefault().unregister(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        schedule_backdrop.setImageDrawable(ResourcesCompat.getDrawable(this,
                R.drawable.superglyph_outline360x114dp))

        val avatarKey = AppPrefs.getInstance(this).avatarKey
        val name = AppPrefs.getInstance(this).name

        if (! TextUtils.isEmpty(avatarKey))
        {
            val callback = object : Callback
            {
                override fun onSuccess()
                {
                    schedule_placeholder_emoji.text = ""
                }

                override fun onError()
                {
                    schedule_placeholder_emoji.text = EmojiUtil.getEmojiForUser(name)
                }
            }

            Picasso.with(this)
                    .load(HTTPS_S3_AMAZONAWS_COM_DROIDCONIMAGES + avatarKey)
                    .placeholder(R.drawable.circle_profile_placeholder)
                    .into(schedule_toolbar_profile, callback)
        }
        else
        {
            schedule_placeholder_emoji.text = EmojiUtil.getEmojiForUser(name)
        }

        appbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (appBarLayout.totalScrollRange > 0) {
                val percentage: Float = 1 - (Math.abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange)
                schedule_toolbar_title.alpha = percentage
                schedule_toolbar_profile.alpha = percentage
                schedule_toolbar_notif.alpha = percentage
                schedule_placeholder_emoji.alpha = percentage
            }
        }
        appbar.setExpanded(true)

        schedule_profile_touch.setOnClickListener {
            launchUserDetail()
        }

        schedule_toolbar_notif.setOnClickListener {
            val prefs = AppPrefs.getInstance(this)
            updateNotifications(!prefs.allowNotifications)
        }
    }

    private fun setupNavigationDrawer() {
        val drawerToggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.setDrawerListener(drawerToggle)

        drawerToggle.syncState()

        drawer_recycler.adapter = DrawerAdapter(getDrawerItems(), object : DrawerClickListener {
            override fun onNavigationItemClick(position: Int, titleRes: Int) {
                drawer_layout.closeDrawer(drawer_recycler)

                when (titleRes) {
                    R.string.explore -> {
                        allEvents = true
                        appbar.setExpanded(true)
                    }
                    R.string.my_schedule -> {
                        allEvents = false
                        appbar.setExpanded(true)
                    }
                    R.string.buy_tickets -> {
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(getString(R.string.buy_ticket_url))
                        startActivity(i)
                    }

                    R.string.social -> FindUserKot.startMe(this@ScheduleActivity)
                    R.string.profile -> createEditUserProfile(this@ScheduleActivity)
                    R.string.sponsors -> startActivity(WelcomeActivity.getLaunchIntent(this@ScheduleActivity,
                            true))
                    R.string.about -> AboutActivity.callMe(this@ScheduleActivity)
                }

                Handler().post(RefreshRunnable())
                (drawer_recycler.adapter as DrawerAdapter).setSelectedPosition(position)
                adjustToolBarAndDrawers()
            }

            override fun onHeaderItemClick() {
                launchUserDetail()
            }
        })
        drawer_recycler.layoutManager = LinearLayoutManager(this)
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, filter_wrapper)
    }

    private fun getDrawerItems(): List<Any> {
        val drawerItems = ArrayList<Any>()
        drawerItems.add("header_placeholder")
        drawerItems.add(NavigationItem(R.string.explore, R.drawable.vic_event_black_24dp))
        drawerItems.add(NavigationItem(R.string.my_schedule, R.drawable.vic_clock_black_24dp))
        drawerItems.add(NavigationItem(R.string.profile, R.drawable.vic_account_circle_black_24dp))
        drawerItems.add("divider_placeholder")
        drawerItems.add(NavigationItem(R.string.buy_tickets, R.drawable.ic_action_ticket))
        drawerItems.add(NavigationItem(R.string.about, R.drawable.vic_info_outline_black_24dp))
        return drawerItems
    }

    private fun adjustToolBarAndDrawers() {
        if (allEvents) {
            (drawer_recycler.adapter as DrawerAdapter).setSelectedPosition(POSITION_EXPLORE)
            schedule_toolbar_title.setText(R.string.app_name)
            schedule_backdrop.setColorFilter(ContextCompat.getColor(this, R.color.glyph_foreground_dark))
            schedule_backdrop.setBackgroundColor(ContextCompat.getColor(this, R.color.glyph_background_dark))
            schedule_toolbar_title.setTextColor(ContextCompat.getColor(this, R.color.tab_text_dark))
            tabs.setTabTextColors(ContextCompat.getColor(this, R.color.tab_inactive_text_dark), ContextCompat.getColor(this, R.color.tab_text_dark))
            tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.tab_accent_dark))
            val menuIconDark = toolbar.navigationIcon?.mutate()
            menuIconDark?.mutate()?.setColorFilter(ContextCompat.getColor(this, R.color.tab_text_dark), PorterDuff.Mode.SRC_IN)
            menuIconDark?.alpha = ALPHA_OPAQUE
            toolbar.navigationIcon = menuIconDark

            schedule_toolbar_notif.visibility = View.GONE
        } else {
            (drawer_recycler.adapter as DrawerAdapter).setSelectedPosition(POSITION_MY_SCHEDULE)
            schedule_toolbar_title.setText(R.string.my_schedule)
            schedule_backdrop.setColorFilter(ContextCompat.getColor(this, R.color.glyph_foreground_light))
            schedule_backdrop.setBackgroundColor(ContextCompat.getColor(this, R.color.glyph_background_light))
            schedule_toolbar_title.setTextColor(ContextCompat.getColor(this, R.color.tab_text_light))
            tabs.setTabTextColors(ContextCompat.getColor(this, R.color.tab_inactive_text_light), ContextCompat.getColor(this, R.color.tab_text_light))
            tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.tab_accent_light))
            val menuIconLight = toolbar.navigationIcon?.mutate()
            menuIconLight?.setColorFilter(ContextCompat.getColor(this, R.color.tab_text_light), PorterDuff.Mode.SRC_IN)
            menuIconLight?.alpha = ALPHA_OPAQUE
            toolbar.navigationIcon = menuIconLight

            schedule_toolbar_notif.visibility = View.VISIBLE
        }

        if(AppPrefs.getInstance(this).allowNotifications)
            schedule_toolbar_notif.setImageResource(R.drawable.vic_notifications_active_black_24dp)
        else
            schedule_toolbar_notif.setImageResource(R.drawable.vic_notifications_none_black_24dp)
    }

    private fun updateNotifications(allow:Boolean)
    {
        val prefs = AppPrefs.getInstance(this)
        prefs.allowNotifications = allow
        prefs.showNotifCard = false
        (view_pager.adapter as ScheduleFragmentPagerAdapter).updateNotifCard()
        Queues.localQueue(this).execute(UpdateAlertsTask())
        adjustToolBarAndDrawers()
    }

    private fun launchUserDetail() {
        val userId = AppPrefs.getInstance(this).userId
        if (userId != null) {
            val ua = DatabaseHelper.getInstance(this).userAccountDao.queryForId(
                    userId)
            if (ua != null && ua.userCode != null && !TextUtils.isEmpty(ua.userCode)) {
                drawer_layout.closeDrawer(drawer_recycler)
                UserDetailActivity.callMe(this, ua.userCode)
            }
        }
    }

    private fun initNfc() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter.setNdefPushMessageCallback(this, this)
    }

    override fun createNdefMessage(event: NfcEvent?): NdefMessage? {
        val userId = AppPrefs.getInstance(this).userId
        if (userId != null) {
            val ua = DatabaseHelper.getInstance(this).userAccountDao.queryForId(
                    userId)
            if (ua != null && ua.userCode != null && !TextUtils.isEmpty(ua.userCode)) {
                val msg = NdefMessage(arrayOf(NdefRecord.createMime("application/vnd.co.touchlab.droidconandroid",
                        ua.userCode.toByteArray())
                        , NdefRecord.createApplicationRecord("co.touchlab.droidconandroid")))
                return msg
            }
        }
        return null
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun onEventMainThread(eventDetailTask: RefreshScheduleData) {
        Handler().post(RefreshRunnable())
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun onEventMainThread(command: UploadAvatarCommand) {
        drawer_recycler.adapter.notifyDataSetChanged()
        setupToolbar()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun onEventMainThread(command: UploadCoverCommand) {
        drawer_recycler.adapter.notifyDataSetChanged()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    fun onEventMainThread(notificationEvent: UpdateAllowNotificationEvent) {
        //Have to handle the notification card way out here so it can update both fragments.
        //Set the app prefs and bounce it back down to the adapter
        updateNotifications(notificationEvent.allow)
    }

    class ConfHost : ConferenceDataHost {
        override fun loadCallback(conferenceDayHolders: Array<out ConferenceDayHolder>?) {
            EventBusExt.getDefault().post(conferenceDayHolders)
        }
    }

    inner class RefreshRunnable() : Runnable {
        override fun run() {
            conferenceDataPresenter?.unregister()
            conferenceDataPresenter = ConferenceDataPresenter(this@ScheduleActivity,
                    ConfHost(),
                    allEvents)

            val dates: ArrayList<Long> = ArrayList()
            val startString: String? = AppPrefs.getInstance(this@ScheduleActivity).conventionStartDate
            val endString: String? = AppPrefs.getInstance(this@ScheduleActivity).conventionEndDate

            if (!TextUtils.isEmpty(startString) && !TextUtils.isEmpty(endString)) {
                var start: Long = TimeUtils.sanitize(TimeUtils.DATE_FORMAT.get().parse(startString))
                val end: Long = TimeUtils.sanitize(TimeUtils.DATE_FORMAT.get().parse(endString))

                while (start <= end) {
                    dates.add(start)
                    start += DateUtils.DAY_IN_MILLIS
                }

                view_pager.adapter = ScheduleFragmentPagerAdapter(
                        supportFragmentManager,
                        dates,
                        allEvents)
                view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
                tabs.setupWithViewPager(view_pager)
            }
        }
    }

    class ScheduleFragmentPagerAdapter : FragmentStatePagerAdapter {
        private var dates: List<Long>
        private var allEvents: Boolean
        private var fragmentManager: FragmentManager

        private val tabDateFormat = SimpleDateFormat("MMM dd", Locale.US)

        constructor(fm: FragmentManager, dates: List<Long>, allEvents: Boolean) : super(fm) {
            this.dates = dates
            this.allEvents = allEvents
            this.fragmentManager = fm
        }

        override fun getCount(): Int {
            return dates.size
        }

        override fun getItem(position: Int): ScheduleDataFragment? {
            return createScheduleDataFragment(allEvents, dates[position], position)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return tabDateFormat.format(Date(dates[position]))
        }

        fun updateFrags(track: Track) {
            for (fragment in fragmentManager.fragments) {
                    (fragment as? ScheduleDataFragment)?.filter(track)
            }
        }

        fun updateNotifCard() {
            for (fragment in fragmentManager.fragments) {
                (fragment as? ScheduleDataFragment)?.updateNotifCard()
            }
        }
    }
}
