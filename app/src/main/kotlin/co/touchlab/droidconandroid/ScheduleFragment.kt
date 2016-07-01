package co.touchlab.droidconandroid

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.droidconandroid.data.AppPrefs
import co.touchlab.droidconandroid.data.Track
import co.touchlab.droidconandroid.presenter.ConferenceDataHost
import co.touchlab.droidconandroid.presenter.ConferenceDataPresenter
import co.touchlab.droidconandroid.presenter.ConferenceDayHolder
import co.touchlab.droidconandroid.tasks.persisted.RefreshScheduleData
import co.touchlab.droidconandroid.ui.UpdateAllowNotificationEvent
import co.touchlab.droidconandroid.utils.TimeUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Created by izzyoji :) on 8/5/15.
 */
class ScheduleFragment : Fragment(), FilterableFragmentInterface
{
    var conferenceDays: Array<ConferenceDayHolder>? = null
    var conferenceDataPresenter: ConferenceDataPresenter? = null

    companion object
    {
        val ALL_EVENTS = "all_events"

        val EXPLORE = "EXPLORE"
        val MY_SCHEDULE = "MY_SCHEDULE"

        private var pagerAdapter: ScheduleFragmentPagerAdapter? = null
        val tabDateFormat = SimpleDateFormat("MMM dd")

        fun newInstance(all: Boolean): ScheduleFragment
        {
            val fragment = ScheduleFragment()
            val args = Bundle()
            args.putBoolean(ALL_EVENTS, all)
            fragment.setArguments(args)
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater!!.inflate(R.layout.fragment_schedule, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Fragment>.onCreate(savedInstanceState)
        EventBusExt.getDefault().register(this)
        conferenceDataPresenter = ConferenceDataPresenter(activity, ConfHost(), getArguments().getBoolean(ALL_EVENTS))
    }

    class ConfHost: ConferenceDataHost
    {
        override fun loadCallback(conferenceDayHolders: Array<out ConferenceDayHolder>?) {
            EventBusExt.getDefault()!!.post(conferenceDayHolders)
        }
    }

    override fun onDestroy() {
        super<Fragment>.onDestroy()
        EventBusExt.getDefault().unregister(this)
        conferenceDataPresenter!!.unregister()
    }

    public fun onEventMainThread(eventDetailTask: RefreshScheduleData)
    {
//        Handler().post(RefreshRunnable())
    }

    fun onEventMainThread(notificationEvent: UpdateAllowNotificationEvent) {
        //Have to handle the notification card way out here so it can update both fragments.
        //Set the app prefs and bounce it back down to the adapter
        val prefs = AppPrefs.getInstance(context)
        prefs.allowNotifications = notificationEvent.allow
        prefs.showNotifCard = false
        pagerAdapter!!.updateNotifCard(false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super<Fragment>.onActivityCreated(savedInstanceState)

        Handler().post(RefreshRunnable())
    }

    inner class RefreshRunnable(): Runnable
    {
        override fun run() {
            val allEvents = getArguments().getBoolean(ALL_EVENTS)
            conferenceDataPresenter!!.refreshConferenceData()
            val pager = view?.findViewById(R.id.pager)!! as ViewPager
            val tabs = view?.findViewById(R.id.tabs)!! as TabLayout
            val tabWrapper = view?.findViewById(R.id.tabs_wrapper)

            if (allEvents) {
                tabWrapper?.setBackgroundColor(getResources().getColor(R.color.primary))
            } else {
                tabWrapper?.setBackgroundColor(getResources().getColor(R.color.blue_grey))
            }

            val dates: ArrayList<Long> = ArrayList<Long>()
            val startString: String? = AppPrefs.getInstance(getActivity()).getConventionStartDate()
            val endString: String? = AppPrefs.getInstance(getActivity()).getConventionEndDate()

            if (!TextUtils.isEmpty(startString) && !TextUtils.isEmpty(endString)) {
                var start: Long = TimeUtils.sanitize(TimeUtils.DATE_FORMAT.get().parse(startString))
                val end: Long = TimeUtils.sanitize(TimeUtils.DATE_FORMAT.get().parse(endString))

                while (start <= end) {
                    dates.add(start)
                    start += DateUtils.DAY_IN_MILLIS
                }

                pagerAdapter = ScheduleFragmentPagerAdapter(getChildFragmentManager(), dates, allEvents)
                pager.setAdapter(pagerAdapter);
                pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
                tabs.setTabsFromPagerAdapter(pagerAdapter!!)
                tabs.setOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(pager))
            }
        }
    }

    override fun applyFilters(track: Track) {
        pagerAdapter!!.updateFrags(track)
    }
}

class ScheduleFragmentPagerAdapter : FragmentPagerAdapter
{
    private var dates: List<Long>
    private var allEvents: Boolean
    private var fragmentManager: FragmentManager

    constructor(fm: FragmentManager, dates: List<Long>, allEvents: Boolean) : super(fm) {
        this.dates = dates;
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
        return ScheduleFragment.tabDateFormat.format(Date(dates.get(position)))
    }

    fun updateFrags(track: Track) {

        for (fragment in fragmentManager.getFragments()) {
            if(fragment != null) {
                (fragment as ScheduleDataFragment).filter(track)
            }
        }
    }

    fun  updateNotifCard(show: Boolean) {
        for (fragment in fragmentManager.fragments) {
                (fragment as ScheduleDataFragment).updateNotifCard(show)
        }
    }
}