package co.touchlab.droidconandroid

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.droidconandroid.data.Event
import co.touchlab.droidconandroid.data.Track
import co.touchlab.droidconandroid.data.UserAccount
import co.touchlab.droidconandroid.presenter.EventDetailHost
import co.touchlab.droidconandroid.presenter.EventDetailPresenter
import co.touchlab.droidconandroid.tasks.AddRsvpTask
import co.touchlab.droidconandroid.tasks.Queues
import co.touchlab.droidconandroid.tasks.RemoveRsvpTask
import co.touchlab.droidconandroid.tasks.TrackDrawableTask
import com.wnafee.vector.compat.ResourcesCompat
import kotlinx.android.synthetic.main.fragment_event_detail.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by kgalligan on 7/27/14.
 */
var businessDrawable: Drawable? = null
var designDrawable: Drawable? = null
var devDrawable: Drawable? = null

class EventDetailFragment() : Fragment()
{
    private var trackColor: Int = 0
    private var fabColorList: ColorStateList? = null
    private var presenter: EventDetailPresenter? = null

    companion object
    {
        val EVENT_ID = "EVENT_ID"
        val TRACK_ID = "TRACK_ID"

        fun createFragment(id: Long, track: Int): EventDetailFragment
        {
            val bundle = Bundle()
            bundle.putLong(EVENT_ID, id)
            bundle.putInt(TRACK_ID, track)

            val f = EventDetailFragment()
            f.arguments = bundle

            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        EventBusExt.getDefault() !!.register(this)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        EventBusExt.getDefault() !!.unregister(this)
    }

    private fun findEventIdArg(): Long
    {
        var eventId = arguments?.getLong(EVENT_ID, - 1)
        if (eventId == null || eventId == - 1L)
        {
            if (activity == null)
                return - 1L

            eventId = activity !!.intent !!.getLongExtra(EVENT_ID, - 1)
        }

        if (eventId == - 1L)
            throw IllegalArgumentException("Must set event id")

        return eventId
    }

    /**
     * Gets the track ID argument. This is to make sure we don't flash the incorrect colors
     * on things like the FAB and toolbar while waiting to load the event details
     */
    private fun findTrackIdArg(): String?
    {
        var trackId = arguments?.getString(TRACK_ID)
        if (trackId == null)
        {
            trackId = activity.intent.getStringExtra(TRACK_ID)
        }

        return trackId
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        presenter = EventDetailPresenter(context, findEventIdArg(), EventDetailHost {
            dataRefresh()
        })

        return inflater !!.inflate(R.layout.fragment_event_detail, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as AppCompatActivity
        toolbar.title = ""
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setDisplayShowHomeEnabled(true)
        recycler.layoutManager = LinearLayoutManager(getActivity())

        updateTrackColor(findTrackIdArg())
    }

    fun dataRefresh()
    {
        if (! presenter !!.eventDetailLoadTask.eventId.equals(findEventIdArg()))
            return

        val event = presenter !!.eventDetailLoadTask.event !!

        updateTrackColor(event.category)
        updateFAB(event)

        updateContent(event,
                presenter !!.eventDetailLoadTask.speakers,
                presenter !!.eventDetailLoadTask.conflict)
    }

    fun onEventMainThread(task: TrackDrawableTask)
    {
        when (task.drawableRes)
        {
            R.drawable.illo_development ->
            {
                devDrawable = task.drawable
            }

            R.drawable.illo_design ->
            {
                designDrawable = task.drawable
            }

            R.drawable.illo_business ->
            {
                businessDrawable = task.drawable
            }
        }

        if (task.drawable != null)
            updateBackdropDrawable(task.drawable !!)
    }

    /**
     * Sets up the floating action bar according to the event details. This includes setting the color
     * and adjusting the icon according to rsvp status
     */
    private fun updateFAB(event: Event)
    {
        //Follow Fab
        fab.backgroundTintList = fabColorList
        fab.setColorFilter(trackColor)
        fab.setRippleColor(ContextCompat.getColor(context, R.color.white))

        if (event.isRsvped)
        {
            fab.setImageDrawable(ResourcesCompat.getDrawable(activity, R.drawable.ic_check))
            fab.isActivated = true
        }
        else
        {
            fab.setImageDrawable(ResourcesCompat.getDrawable(activity, R.drawable.ic_plus))
            fab.isActivated = false
        }

        if (! event.isPast)
        {
            fab.setOnClickListener { v ->
                if (event.isRsvped)
                {
                    Queues.localQueue(activity).execute(RemoveRsvpTask(event.id))
                }
                else
                {
                    Queues.localQueue(activity).execute(AddRsvpTask(event.id))
                }
            }
        }

        if (event.isNow)
        {
            fab.setOnLongClickListener { v ->
                val intent = Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://imgur.com/gallery/7drHiqr"))
                if (intent.resolveActivity(activity.packageManager) != null)
                {
                    activity.startActivity(intent)
                }
                true
            }
        }

        val p = fab.layoutParams as CoordinatorLayout.LayoutParams
        if (event.isPast)
        {
            p.anchorId = View.NO_ID
            fab.layoutParams = p
            fab.visibility = View.GONE
        }
        else
        {
            p.anchorId = R.id.appbar
            fab.layoutParams = p
            fab.visibility = View.VISIBLE
        }
    }

    /**
     * Adds all the content to the recyclerView
     */
    private fun updateContent(event: Event, speakers: List<UserAccount>?, conflict: Boolean)
    {
        val adapter = EventDetailAdapter(activity, trackColor)

        //Construct the time and venue string and add it to the adapter
        val startDateVal = Date(event.startDateLong !!)
        val endDateVal = Date(event.endDateLong !!)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val venueFormatString = resources.getString(R.string.event_venue_time)

        var formattedStart = timeFormat.format(startDateVal)
        val formattedEnd = timeFormat.format(endDateVal)

        val startMarker = formattedStart.substring(Math.max(formattedStart.length - 3, 0))
        val endMarker = formattedEnd.substring(Math.max(formattedEnd.length - 3, 0))

        if (TextUtils.equals(startMarker, endMarker))
        {
            formattedStart = formattedStart.substring(0, Math.max(formattedStart.length - 3, 0))
        }

        adapter.addHeader(event.name, venueFormatString.format(event.venue.name, formattedStart, formattedEnd))
        adapter.addStream("http://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8")

        //TODO add live stream link
        //adapter.addStream("live stream link goes here")

        if (event.isNow)
            adapter.addInfo("<i><b>" + resources.getString(R.string.event_now) + "</b></i>")
        else if (event.isPast)
            adapter.addInfo("<i><b>" + resources.getString(R.string.event_past) + "</b></i>")
        else if (conflict)
            adapter.addInfo("<i><b>" + resources.getString(R.string.event_conflict) + "</b></i>")

        //Description text
        if (! TextUtils.isEmpty(event.description))
            adapter.addBody(event.description)

        for (item: UserAccount in speakers as ArrayList)
        {
            adapter.addSpeaker(item)
        }

        //TODO add feedback link
        //adapter.addFeedback("feedback link goes here")

        recycler.adapter = adapter
    }

    /**
     * Ensures that all view which are colored according to the track are updated
     */
    private fun updateTrackColor(category: String?)
    {
        //Default to design
        val track = if (! TextUtils.isEmpty(category)) Track.findByServerName(category)
        else Track.findByServerName("Design")

        //TODO add new backdrop assets
//        var backdropDrawable: Drawable? = null
//
//        when (track)
//        {
//            Track.DEVELOPMENT ->
//            {
//                if (devDrawable == null)
//                    TaskQueue.loadQueueDefault(activity).execute(TrackDrawableTask(activity.applicationContext,
//                            R.drawable.illo_development))
//                else
//                    backdropDrawable = devDrawable
//            }
//
//            Track.DESIGN ->
//            {
//                if (designDrawable == null)
//                    TaskQueue.loadQueueDefault(activity).execute(TrackDrawableTask(activity.applicationContext,
//                            R.drawable.illo_design))
//                else
//                    backdropDrawable = designDrawable
//            }
//            Track.BUSINESS ->
//            {
//                if (businessDrawable == null)
//                    TaskQueue.loadQueueDefault(activity).execute(TrackDrawableTask(activity.applicationContext,
//                            R.drawable.illo_business))
//                else
//                    backdropDrawable = businessDrawable
//            }
//        }
//
//        if (backdropDrawable != null)
//            updateBackdropDrawable(backdropDrawable)

        trackColor = ContextCompat.getColor(context,
                context.resources.getIdentifier(track.textColorRes,
                        "color",
                        context.packageName))
        fabColorList = ContextCompat.getColorStateList(context,
                context.resources.getIdentifier(track.checkBoxSelectorRes,
                        "color",
                        context.packageName))

        collapsingToolbar.setContentScrimColor(trackColor)
        collapsingToolbar.setStatusBarScrimColor(trackColor)
        backdrop.setBackgroundColor(trackColor)
    }

    private fun updateBackdropDrawable(backdropDrawable: Drawable)
    {
        backdrop.setImageDrawable(backdropDrawable)
    }
}