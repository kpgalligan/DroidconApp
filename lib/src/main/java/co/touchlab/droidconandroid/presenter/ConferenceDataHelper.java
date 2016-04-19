package co.touchlab.droidconandroid.presenter;
import android.content.Context;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import co.touchlab.droidconandroid.data.Block;
import co.touchlab.droidconandroid.data.DatabaseHelper;
import co.touchlab.droidconandroid.data.Event;
import co.touchlab.droidconandroid.data.ScheduleBlock;
import co.touchlab.squeaky.dao.Dao;

/**
 * Created by kgalligan on 4/17/16.
 */
public class ConferenceDataHelper
{
    static SimpleDateFormat df         = new SimpleDateFormat("MM/dd/yyyy hh:mma");
    static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mma");

    public static ConferenceDayHolder[] listDays(Context context/*, Convention convention*/) throws SQLException
    {
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        final Dao<Event> eventDao = databaseHelper.getEventDao();
        final Dao<Block> blockDao = databaseHelper.getBlockDao();

        List<ScheduleBlock> all = new ArrayList<>();

        all.addAll(blockDao.queryForAll().list());
        all.addAll(eventDao.queryForAll().list());

        /*for(Venue venue : convention.venues)
        {
            all.addAll(venue.events);
            for(Event event : venue.events)
            {
                event.venue = venue;
            }
        }*/

        Collections.sort(all, new Comparator<ScheduleBlock>()
        {
            @Override
            public int compare(ScheduleBlock o1, ScheduleBlock o2)
            {
                final long compTimes = o1.getStartLong() - o2.getStartLong();
                if(compTimes != 0) return compTimes > 0
                        ? 1
                        : - 1;

                if(o1.isBlock() && o2.isBlock()) return 0;

                if(o1.isBlock()) return 1;
                if(o2.isBlock()) return - 1;

                return ((Event) o1).venue.name.compareTo(((Event) o2).venue.name);
            }
        });

        TreeMap<String, TreeMap<String, List<ScheduleBlock>>> allTheData = new TreeMap<>();

        for(ScheduleBlock scheduleBlock : all)
        {
            final Date startDateObj = new Date(scheduleBlock.getStartLong());
            final String startDate = dateFormat.format(startDateObj);
            TreeMap<String, List<ScheduleBlock>> stringListTreeMap = allTheData
                    .get(startDate);
            if(stringListTreeMap == null)
            {
                stringListTreeMap = new TreeMap<>();
                allTheData.put(startDate, stringListTreeMap);
            }

            final String startTime = timeFormat.format(startDateObj);
            List<ScheduleBlock> timeBlocks = stringListTreeMap.get(startTime);
            if(timeBlocks == null)
            {
                timeBlocks = new ArrayList<>();
                stringListTreeMap.put(startTime, timeBlocks);
            }

            timeBlocks.add(scheduleBlock);
        }

        List<ConferenceDayHolder> dayHolders = new ArrayList<>();

        for(String dateString : allTheData.keySet())
        {
            final TreeMap<String, List<ScheduleBlock>> hourBlocksMap = allTheData
                    .get(dateString);

            List<ConferenceHourHolder> hoursList = new ArrayList<>();
            for(String hourString : hourBlocksMap.keySet())
            {
                final ConferenceHourHolder conferenceHourHolder = new ConferenceHourHolder(
                        hourString, hourBlocksMap.get(hourString).toArray(
                        new ScheduleBlock[hourBlocksMap.get(hourString).size()]));
                hoursList.add(conferenceHourHolder);
            }

            final ConferenceDayHolder conferenceDayHolder = new ConferenceDayHolder(dateString, hoursList.toArray(new ConferenceHourHolder[hoursList.size()]));
            dayHolders.add(conferenceDayHolder);
        }

        return dayHolders.toArray(new ConferenceDayHolder[dayHolders.size()]);
    }
}
