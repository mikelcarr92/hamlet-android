package com.twopicode.hamlet.searchresult;

import com.ocpsoft.pretty.time.PrettyTime;
import org.joda.time.DateTime;
import java.util.Date;

/****************************************
 * Created by michaelcarr on 28/11/15.
 ****************************************/
public class Note extends NoteTask {

    public String getLastUpdatedTime() {
        DateTime lastUpdatedDateTime = getLastUpdatedDateTime();
        PrettyTime prettyTime = new PrettyTime();
        return prettyTime.format(new Date(lastUpdatedDateTime.getMillis()));
    }

}
