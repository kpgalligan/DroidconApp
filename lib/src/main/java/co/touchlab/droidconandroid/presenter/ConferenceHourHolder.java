package co.touchlab.droidconandroid.presenter;
import co.touchlab.droidconandroid.data.ScheduleBlock;

/**
 * Created by kgalligan on 4/17/16.
 */
public class ConferenceHourHolder
{
    public final String hourString;
    public final ScheduleBlock[] scheduleBlocks;

    public ConferenceHourHolder(String hourString, ScheduleBlock[] scheduleBlocks)
    {
        this.hourString = hourString;
        this.scheduleBlocks = scheduleBlocks;
    }
}
