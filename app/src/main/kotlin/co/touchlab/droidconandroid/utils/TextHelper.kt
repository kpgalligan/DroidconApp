package co.touchlab.droidconandroid.utils

import java.util.regex.Pattern
import org.apache.commons.lang3.StringUtils

/**
 * Created by kgalligan on 7/27/14.
 */
class TextHelper
{
    class object
    {
        fun findTagLinks(s: String): String
        {
            if(StringUtils.isEmpty(s))
                return ""

            val regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
            val result = s.replaceAll(regex, { match -> "<a href='" + match.group() + "'>" + match.group() + "</a>" })
            return result
        }
    }
}