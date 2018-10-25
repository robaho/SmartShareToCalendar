package com.robaho.smartsharecalendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.util.Log;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyShareActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String action = intent.getAction();

        super.onCreate(savedInstanceState);

        Log.d("MyShareActivity", "onCreate intent " + intent);
        if (Intent.ACTION_SEND.equals(action)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                handleSharedText(sharedText);
            }
            setResult(RESULT_OK);
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
    }

    private Pattern titleP,whereP,dateP,timeP;

    private void handleSharedText(String text) {
        String title = null;
        String where = null;
        String when = null;
        String time = null;

        if(titleP==null)
            titleP = Pattern.compile("(title|what|event)[\\s:]*(.+)[\\n\\r]*", Pattern.CASE_INSENSITIVE);
        Matcher m = titleP.matcher(text);
        if (m.find()) {
            title = m.group(2);
        }

        if(whereP==null)
            whereP = Pattern.compile("(where|location|place)[\\s:]*(.+)[\\n\\r]*", Pattern.CASE_INSENSITIVE);
        m = whereP.matcher(text);
        if (m.find()) {
            where = m.group(2);
        }

        if(dateP==null)
            dateP = Pattern.compile("(when|date)[\\s:]*(.+)[\\n\\r]*", Pattern.CASE_INSENSITIVE);
        m = dateP.matcher(text);
        if (m.find()) {
            when = m.group(2);
        }

        if(timeP==null)
            timeP = Pattern.compile("(time|at)[\\s:]*(.+)[\\n\\r]*", Pattern.CASE_INSENSITIVE);
        m = timeP.matcher(text);
        if (m.find()) {
            time = m.group(2);
        }

        if (when == null && time != null) {
            when = time;
            time = null;
        }

        if (when == null)
            when = "";

        String desc = text;

        Log.d("MyShareActivity", "the text is [" + text + "]");
        Log.d("MyShareActivity", "title = " + title);
        Log.d("MyShareActivity", "where = " + where);
        Log.d("MyShareActivity", "when = " + when);
        Log.d("MyShareActivity", "time = " + time);

        String full = when + (time != null ? " " + time : "");

        Parser pr = new Parser();
        List<DateGroup> groups = pr.parse(full);

        if (groups.size() == 0) {
            // search all of the text for a date(s)
            groups = pr.parse(text);
        }

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setType("vnd.android.cursor.item/event");
//        .setData(Events.CONTENT_URI)

        if (groups.size() > 0) {
            DateGroup group = groups.get(0);
            long start = group.getDates().get(0).getTime();
            long end;
            if (group.getDates().size() > 1) {
                end = group.getDates().get(1).getTime();
            } else {
                end = start + 60 * 60 * 1000;
            }

//    		Calendar now = Calendar.getInstance();
//    		now.setTimeInMillis(System.currentTimeMillis());
//    		
//    		Calendar cal = Calendar.getInstance();
//    		cal.setTimeInMillis(start);
//    		if(cal.get(Calendar.YEAR)<now.get(Calendar.YEAR)){
//    			cal.set
//    			
//    		}

            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end);

            if (group.isTimeInferred()) {
                intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
            }
        }

        if (title == null) {
            if (groups.size() == 0 || groups.get(0).getLine() > 0) {
                Pattern p = Pattern.compile("\\A(.*)[,\\.\\r\\n]*", Pattern.CASE_INSENSITIVE);
                m = p.matcher(text);
                if (m.find()) {
                    title = m.group(1);
                }
                p = Pattern.compile("\\A\\s*(where|when|title)", Pattern.CASE_INSENSITIVE);
                m = p.matcher(title);
                if (m.find()) {
                    title = null;
                }
            }
        }


        if (title != null)
            intent.putExtra(Events.TITLE, title);

        intent.putExtra(Events.DESCRIPTION, desc);

        if (where != null)
            intent.putExtra(Events.EVENT_LOCATION, where);

//        .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)
//        .putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com");
        startActivity(intent);

    }
}
