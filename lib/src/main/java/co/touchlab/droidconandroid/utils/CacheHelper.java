package co.touchlab.droidconandroid.utils;
import android.content.Context;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by kgalligan on 4/8/16.
 */
public class CacheHelper
{
    public static synchronized String findFile(Context c, String name) throws IOException
    {
        File file = locateFile(c, name);
        if (file.exists() && file.length() > 0)
        {
            FileInputStream fileInputStream = new FileInputStream(file);
            try
            {
                List<String> lines = IOUtils.readLines(fileInputStream);
                return StringUtils.join(lines, "\n");
            }
            finally
            {
                fileInputStream.close();
            }
        }

        return null;
    }

    public static synchronized void saveFile(Context c, String name, String data) throws IOException
    {
        File file = locateFile(c, name);
        File tempFile = new File(file.getParent(), file.getName() + ".tmp");
        if(tempFile.exists())
            tempFile.delete();

        FileWriter out = new FileWriter(tempFile);
        IOUtils.write(data, out);
        out.close();
        if(file.exists())
            file.delete();
        tempFile.renameTo(file);
    }

    private static File locateFile(Context c, String name)
    {
        File filesDir = c.getFilesDir();
        return new File(filesDir, name);
    }
}
