package co.touchlab.droidconandroid.data;

import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.table.DatabaseTable;

/**
 * Created by kgalligan on 7/28/14.
 */
@DatabaseTable
public class TalkSubmission
{

    public TalkSubmission()
    {
    }

    public TalkSubmission(Integer id, Integer vote, String title, String description)
    {
        this.id = id;
        this.vote = vote;
        this.title = title;
        this.description = description;
    }


    @DatabaseField(id = true)
    public Integer id;

//    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true)
//    public UserAccount userAccount;

    @DatabaseField
    public Integer vote;

    @DatabaseField
    public String title;

    @DatabaseField
    public String description;

}
