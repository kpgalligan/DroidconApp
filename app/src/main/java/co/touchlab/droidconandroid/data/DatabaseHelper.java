package co.touchlab.droidconandroid.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import co.touchlab.droidconandroid.data.staff.EventAttendee;
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
    private static final int    VERSION            = VOTE;
    private static DatabaseHelper instance;

    // @reminder Ordering matters, create foreign key dependant classes later
    private final Class[] tableClasses = new Class[] {Venue.class, Event.class, Block.class, Invite.class, UserAccount.class, EventAttendee.class, EventSpeaker.class, TalkSubmission.class};

    private DatabaseHelper(Context context)
    {
        super(context, DATABASE_FILE_NAME, null, VERSION);
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
