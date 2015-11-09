package co.touchlab.droidconandroid.tasks

import android.content.Context
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.android.threading.tasks.Task
import co.touchlab.droidconandroid.data.DatabaseHelper
import co.touchlab.droidconandroid.data.Event
import co.touchlab.squeaky.dao.Dao
import co.touchlab.squeaky.stmt.Where
import java.util.*

/**
 * Created by toidiu on 7/20/14.
 */
class FindVoteDbTaskKot(val openForVote: Boolean) : Task() {
    var list: List<Event> = ArrayList()

    override fun run(context: Context?) {



        val dao: Dao<Event, Long> = DatabaseHelper.getInstance(context).eventDao

        list = when (openForVote) {
            true -> Where<Event, Long>(dao).isNull("vote")!!.query()!!.list()
            false -> Where<Event, Long>(dao).isNotNull("vote")!!.query()!!.list()
        }

    }

    override fun onComplete(context: Context?) {
        EventBusExt.getDefault() post this
    }

    override fun handleError(context: Context?, e: Throwable?): Boolean {
        return true
    }

}
