package co.touchlab.droidconandroid.data;

import java.io.Serializable;
import java.util.Random;

import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.table.DatabaseTable;

/**
 * Created by kgalligan on 7/28/14.
 */
@DatabaseTable public class TalkSubmission implements Serializable
{
    public static Random rand = new Random();

    public static Integer getRandInt()
    {
        return TalkSubmission.rand.nextInt();
    }

    public TalkSubmission()
    {
    }

    public TalkSubmission(Long id, Integer vote, String title, String description, String speaker)
    {
        this.id = id;
        this.vote = vote;
        this.title = title;
        this.description = description;
        this.speaker = speaker;
    }


    @DatabaseField(id = true)
    public Long id;

    @DatabaseField
    public Integer vote;

    @DatabaseField
    public String title;

    @DatabaseField
    public String description;

    @DatabaseField
    public String speaker;

    @DatabaseField
    public Integer random;

}
