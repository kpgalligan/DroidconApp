package co.touchlab.droidconandroid.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import co.touchlab.droidconandroid.data.staff.EventAttendee;
import co.touchlab.droidconandroid.tasks.persisted.RefreshScheduleData;
import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.db.sqlite.SQLiteDatabaseImpl;
import co.touchlab.squeaky.db.sqlite.SqueakyOpenHelper;
import co.touchlab.squeaky.table.TableUtils;

/**
 * Created by kgalligan on 6/28/14.
 */
public class DatabaseHelper extends SqueakyOpenHelper
{

    private static final String DATABASE_FILE_NAME = "droidcon";
    public static final  int    BASELINE           = 3;
    private static final int    VOTE               = 4;
    private static final int    STREAM             = 5;
    private static final int    VERSION            = STREAM;
    private static DatabaseHelper instance;

    // @reminder Ordering matters, create foreign key dependant classes later
    private final Class[] tableClasses = new Class[] {Venue.class, Event.class, Block.class, Invite.class, UserAccount.class, EventAttendee.class, EventSpeaker.class, TalkSubmission.class};

    private Context context;

    private DatabaseHelper(Context context)
    {
        super(context, DATABASE_FILE_NAME, null, VERSION);
        this.context = context;
    }

    @NotNull
    public static synchronized DatabaseHelper getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new DatabaseHelper(context);
        }

        return instance;
    }



    @Override
    public void onCreate(SQLiteDatabase db)
    {
        try
        {
            TableUtils.createTables(new SQLiteDatabaseImpl(db), tableClasses);
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if(oldVersion < VOTE)
        {
            try
            {
                TableUtils.createTables(new SQLiteDatabaseImpl(db), TalkSubmission.class);
            }
            catch(SQLException e)
            {
                throw new RuntimeException(e);
            }

        }

        if(oldVersion < STREAM)
        {
            try
            {
                TableUtils.dropTables(new SQLiteDatabaseImpl(db), false, Event.class);
                TableUtils.createTables(new SQLiteDatabaseImpl(db), Event.class);
            }
            catch(SQLException e)
            {
                throw new RuntimeException(e);
            }

        }

        RefreshScheduleData.callMe(context);
    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @NotNull
    public Dao<Venue> getVenueDao()
    {
        return (Dao<Venue>) getDao(Venue.class);
    }

    @NotNull
    public Dao<Event> getEventDao()
    {
        return (Dao<Event>) getDao(Event.class);
    }

    @NotNull
    public Dao<UserAccount> getUserAccountDao()
    {
        return (Dao<UserAccount>) getDao(UserAccount.class);
    }

    @NotNull
    public Dao<EventSpeaker> getEventSpeakerDao()
    {
        return (Dao<EventSpeaker>) getDao(EventSpeaker.class);
    }

    @NotNull
    public Dao<Block> getBlockDao()
    {
        return (Dao<Block>) getDao(Block.class);
    }

    @NotNull
    public Dao<TalkSubmission> getTalkSubDao()
    {
        return (Dao<TalkSubmission>) getDao(TalkSubmission.class);
    }

    public void deleteEventsNotIn(Set<Long> goodStuff) throws SQLException
    {
        final Dao<Event> eventDao = getEventDao();
        final List<Event> allEvents = eventDao.queryForAll().list();
        final Iterator<Event> iterator = allEvents.iterator();
        while(iterator.hasNext())
        {
            Event event = iterator.next();
            if(goodStuff.contains(event.id))
                iterator.remove();
        }

        if(allEvents.size() > 0)
            eventDao.delete(allEvents);
    }

    /**
     * @param transaction .
     * @throws RuntimeException on {@link SQLException}
     */
    public void performTransactionOrThrowRuntime(Callable<Void> transaction)
    {
        SQLiteDatabase db = getWritableDatabase();
        try
        {
            db.beginTransaction();
            transaction.call();
            db.setTransactionSuccessful();
        }
        catch(Exception e)
        {
            Log.e(DatabaseHelper.class.getName(), e.getMessage());
            throw new RuntimeException(e);
        }
        finally
        {
            db.endTransaction();
        }
    }

    public void inTransaction(final Runnable r)
    {
        performTransactionOrThrowRuntime(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                r.run();
                return null;
            }
        });
    }
}
