package co.touchlab.droidconandroid.data;

import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.table.DatabaseTable;

/**
 * Created by kgalligan on 6/28/14.
 */
@DatabaseTable
public class Venue
{
    @DatabaseField(id = true)
    public long id;

    @DatabaseField(canBeNull = false)
    public String name;

    @DatabaseField
    public String description;

    @DatabaseField
    public String mapImageUrl;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getMapImageUrl()
    {
        return mapImageUrl;
    }

    public void setMapImageUrl(String mapImageUrl)
    {
        this.mapImageUrl = mapImageUrl;
    }
}
