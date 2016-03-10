package com.twopicode.hamlet.searchresult;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/****************************************
 * Created by michaelcarr on 28/11/15.
 ****************************************/
public class Task extends NoteTask {

    DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("MMM dd, YYYY h:mm a");

    public String getRemindMeDate() {
        DateTime remindMeDateTime = getRemindMeDateTime();
        return remindMeDateTime.toString(dateTimeFormat).replace(".", "");
    }

    public String getDueDate() {
        DateTime dueDateTime = getDueDateTime();
        return dueDateTime.toString(dateTimeFormat).replace(".", "");
    }

}
