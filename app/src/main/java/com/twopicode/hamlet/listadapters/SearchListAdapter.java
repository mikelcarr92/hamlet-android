package com.twopicode.hamlet.listadapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.twopicode.hamlet.R;
import com.twopicode.hamlet.searchresult.Note;
import com.twopicode.hamlet.searchresult.Task;

import java.util.ArrayList;

/****************************************
 * Created by michaelcarr on 28/11/15.
 ****************************************/
public class SearchListAdapter extends BaseAdapter {

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_TASK = 1;
    public static final int VIEW_TYPE_NOTE = 2;

    private ArrayList<Object> mListItems;
    private Context mContext;
    private Callback mCallback;

    public SearchListAdapter(Context context, ArrayList<Object> searchItems) {
        mContext = context;
        mListItems = searchItems;
    }

    @Override
    public void notifyDataSetChanged() {
        removeHeaders();
        addHeaders();
        super.notifyDataSetChanged();
    }

    /**
        Add headers to list
     **/
    private void addHeaders() {

        if (mListItems == null || mListItems.size() == 0) { return; }

        if (listContainsNotes())
            mListItems.add(0, mContext.getString(R.string.search_results_notes_header));

        if (listContainsTasks())
            mListItems.add(indexOfFirstTask(), mContext.getString(R.string.search_results_tasks_header));
    }

    private boolean listContainsNotes() {
        for (Object item : mListItems)
            if (item instanceof Note)
                return true;
        return false;
    }

    private boolean listContainsTasks() {
        for (Object item : mListItems)
            if (item instanceof Task)
                return true;
        return false;
    }

    private int indexOfFirstTask() {
        for (Object item : mListItems)
            if (item instanceof Task)
                return mListItems.indexOf(item);
        return 0;
    }

    /**
        Remove headers from list
    **/
    private void removeHeaders() {
        ArrayList<Object> itemsToDelete = new ArrayList<>();

        for (Object item : mListItems)
            if (item instanceof String)
                itemsToDelete.add(item);

        mListItems.removeAll(itemsToDelete);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public int getCount() {
        return mListItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof Note)
            return VIEW_TYPE_NOTE;
        if (getItem(position) instanceof Task)
            return VIEW_TYPE_TASK;
        return VIEW_TYPE_HEADER;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View row = convertView;
        ViewHolder viewHolder = null;
        int type = getItemViewType(position);

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch (type) {
                case VIEW_TYPE_NOTE:
                    viewHolder = new NoteCardViewHolder();
                    row = inflater.inflate(R.layout.activity_search_list_item_note, parent, false);
                    ((NoteCardViewHolder) viewHolder).cardView = (CardView) row.findViewById(R.id.activity_search_list_item_note_card_view);
                    ((NoteCardViewHolder) viewHolder).title = (TextView) row.findViewById(R.id.activity_search_list_item_note_title);
                    ((NoteCardViewHolder) viewHolder).lastUpdated = (TextView) row.findViewById(R.id.activity_search_list_item_note_date_modified);
                    break;

                case VIEW_TYPE_TASK:
                    viewHolder = new TaskCardViewHolder();
                    row = inflater.inflate(R.layout.activity_search_list_item_task, parent, false);
                    ((TaskCardViewHolder) viewHolder).cardView = (CardView) row.findViewById(R.id.activity_search_list_item_task_card_view);
                    ((TaskCardViewHolder) viewHolder).title = (TextView) row.findViewById(R.id.activity_search_list_item_task_title);
                    ((TaskCardViewHolder) viewHolder).subject = (TextView) row.findViewById(R.id.activity_search_list_item_task_subject);
                    ((TaskCardViewHolder) viewHolder).remindMeDate = (TextView) row.findViewById(R.id.activity_search_list_item_task_remind_me);
                    ((TaskCardViewHolder) viewHolder).dueDate = (TextView) row.findViewById(R.id.activity_search_list_item_task_due_date);
                    break;

                case VIEW_TYPE_HEADER:
                    viewHolder = new HeaderViewHolder();
                    row = inflater.inflate(R.layout.activity_search_list_item_header, parent, false);
                    ((HeaderViewHolder) viewHolder).title = (TextView) row.findViewById(R.id.activity_search_list_item_header_title);
                    break;
            }

            row.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) row.getTag();
        }

        switch (type) {

            case VIEW_TYPE_NOTE:

                final Note note = (Note) getItem(position);

                ((NoteCardViewHolder) viewHolder).cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCallback != null)
                            mCallback.onItemPressed(note.url);
                    }
                });

                ((NoteCardViewHolder) viewHolder).title.setText(note.title);
                ((NoteCardViewHolder) viewHolder).lastUpdated.setText(note.getLastUpdatedTime());

                break;

            case VIEW_TYPE_TASK:

                final Task task = (Task) getItem(position);

                ((TaskCardViewHolder) viewHolder).cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCallback != null)
                            mCallback.onItemPressed(task.url);
                    }
                });

                ((TaskCardViewHolder) viewHolder).title.setText(task.title);
                ((TaskCardViewHolder) viewHolder).subject.setText(task.student_group.class_group_subject_name);
                ((TaskCardViewHolder) viewHolder).remindMeDate.setText(task.getRemindMeDate());
                ((TaskCardViewHolder) viewHolder).dueDate.setText(task.getDueDate());

                break;

            case VIEW_TYPE_HEADER:

                String header = (String) getItem(position);
                ((HeaderViewHolder) viewHolder).title.setText(header);

                break;
        }

        return row;
    }

    private static class NoteCardViewHolder extends CardViewHolder {
        public TextView lastUpdated;
    }

    private static class TaskCardViewHolder extends CardViewHolder {
        public TextView subject;
        public TextView remindMeDate;
        public TextView dueDate;
    }

    private static class CardViewHolder extends ViewHolder {
        public CardView cardView;
        public TextView title;
    }

    private static class HeaderViewHolder extends ViewHolder {
        public TextView title;
    }

    private static class ViewHolder {}

    public interface Callback {
        void onItemPressed(String url);
    }
}