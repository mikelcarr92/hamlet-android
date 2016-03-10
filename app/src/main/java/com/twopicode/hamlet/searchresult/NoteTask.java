package com.twopicode.hamlet.searchresult;

import org.joda.time.DateTime;

/****************************************
 * Created by michaelcarr on 29/11/15.
 ****************************************/
public class NoteTask  {

    public String date_created;
    public String due_date;
    public int id;
    public String last_updated;
    public String remind_me_date;
    public StudentGroup student_group;
    public int student_group_id;
    public String title;
    public String url;

    protected DateTime getLastUpdatedDateTime() {
        return DateTime.parse(last_updated);
    }

    protected DateTime getRemindMeDateTime() {
        return DateTime.parse(remind_me_date);
    }

    protected DateTime getDueDateTime() {
        return DateTime.parse(due_date);
    }
}
