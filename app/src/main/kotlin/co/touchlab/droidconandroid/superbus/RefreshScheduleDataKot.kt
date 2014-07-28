package co.touchlab.droidconandroid.superbus

import co.touchlab.android.superbus.CheckedCommand
import co.touchlab.droidconandroid.network.DataHelper
import co.touchlab.droidconandroid.network.RefreshScheduleDataRequest
import android.content.Context
import co.touchlab.android.superbus.errorcontrol.PermanentException
import co.touchlab.android.superbus.errorcontrol.TransientException
import co.touchlab.android.superbus.Command
import co.touchlab.droidconandroid.data.DatabaseHelper
import java.text.SimpleDateFormat
import co.touchlab.droidconandroid.BuildConfig
import android.database.SQLException
import java.text.ParseException
import co.touchlab.droidconandroid.data.UserAccount
import co.touchlab.droidconandroid.data.EventSpeaker
import java.util.concurrent.Callable

/**
 * Created by kgalligan on 7/20/14.
 */
open class RefreshScheduleDataKot : CheckedCommand()
{
    override fun logSummary(): String
    {
        return this.javaClass.getSimpleName()
    }

    override fun same(command: Command): Boolean
    {
        return command is RefreshScheduleDataKot
    }

    override fun callCommand(context: Context)
    {
        val restAdapter = DataHelper.makeRequestAdapter(context)!!
        val request = restAdapter.create(javaClass<RefreshScheduleDataRequest>())!!

        val convention = request.getScheduleData(BuildConfig.CONVENTION_ID)
        if (convention == null)
            throw PermanentException("No convention results")

        val databaseHelper = DatabaseHelper.getInstance(context)
        databaseHelper.performTransactionOrThrowRuntime (object : Callable<Void>
        {
            throws(javaClass<Exception>())
            override fun call(): Void?
            {
                val eventDao = databaseHelper.getEventDao()
                val venueDao = databaseHelper.getVenueDao()
                val userAccountDao = databaseHelper.getUserAccountDao()
                val eventSpeakerDao = databaseHelper.getEventSpeakerDao()
                val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mma")

                val venues = convention.venues

                try
                {
                    for (venue in venues)
                    {
                        venueDao.createOrUpdate(venue)
                        for (event in venue.events.iterator())
                        {
                            val dbEvent = eventDao.queryForId(event.id)
                            event.venue = venue
                            event.startDateLong = dateFormat.parse(event.startDate)!!.getTime()
                            event.endDateLong = dateFormat.parse(event.endDate)!!.getTime()

                            if (dbEvent != null)
                                event.rsvpUuid = dbEvent.rsvpUuid

                            eventDao.createOrUpdate(event)

                            val iterator = event.speakers?.iterator()
                            if (iterator != null)
                            {
                                var speakerCount = 0

                                for (ua in iterator)
                                {
                                    var userAccount = userAccountDao.queryForId(ua.id)

                                    if (userAccount == null)
                                    {
                                        userAccount = UserAccount()
                                    }

                                    userAccount!!.id = ua.id
                                    userAccount!!.uuid = ua.uuid
                                    userAccount!!.name = ua.name
                                    userAccount!!.profile = ua.profile
                                    userAccount!!.avatarKey = ua.avatarKey
                                    userAccount!!.userCode = ua.userCode
                                    userAccount!!.company = ua.company
                                    userAccount!!.twitter = ua.twitter
                                    userAccount!!.linkedIn = ua.linkedIn
                                    userAccount!!.website = ua.website

                                    userAccountDao.createOrUpdate(userAccount)

                                    val resultList = eventSpeakerDao.queryBuilder()!!
                                            .where()!!
                                            .eq("event_id", event.id)!!
                                            .and()!!
                                            .eq("userAccount_id", userAccount!!.id)!!
                                            .query()!!

                                    var eventSpeaker = if (resultList.size == 0) EventSpeaker() else resultList[0]

                                    eventSpeaker.event = event
                                    eventSpeaker.userAccount = userAccount
                                    eventSpeaker.displayOrder = speakerCount++

                                    eventSpeakerDao.createOrUpdate(eventSpeaker)
                                }
                            }
                        }
                    }
                }
                catch (e: SQLException)
                {
                    throw PermanentException(e)
                }
                catch (e: ParseException)
                {
                    throw PermanentException(e)
                }

                return null
            }
        })


    }

    override fun handlePermanentError(context: Context, exception: PermanentException): Boolean
    {
        return false
    }
}