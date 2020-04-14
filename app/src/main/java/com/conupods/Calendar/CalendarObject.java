package com.conupods.Calendar;

import java.time.temporal.JulianFields;
import java.util.Calendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import com.conupods.App;

public class CalendarObject {

    private static final String TAG = "CALENDAR_OBJECT";

    private String mCalendarID;
    private String mDisplayName;
    private String mAccountName;
    private String mOwnerName;
    private String mNextEventTitle;
    private String mNextEventLocation;
    private String mNextEventStartTime;
    private String mNextEventEndTime;
    private String mNextEventDate;

    private Cursor mEventCursor;

    // indexes for query result (projection array)
    private static final int PROJECTION_TITLE_INDEX = 0;
    private static final int PROJECTION_START_DAY_INDEX = 1;
    private static final int PROJECTION_START_MINUTE_INDEX = 2;
    private static final int PROJECTION_END_MINUTE_INDEX = 3;
    private static final int PROJECTION_EVENT_LOCATION_INDEX = 4;

    public CalendarObject(String calendarID, String displayName, String accountName, String ownerName) {
        this.mCalendarID = calendarID;
        this.mDisplayName = displayName;
        this.mAccountName = accountName;
        this.mOwnerName = ownerName;
    }

    /*
    initializes instance variables
    returns true if there is a next event and false otherwise
     */
    public Boolean loadNextEvent() {
        boolean hasNextEvent;
        initEventCursor();

        if (mEventCursor !=null){
            mEventCursor.moveToNext();
            mNextEventTitle = mEventCursor.getString(PROJECTION_TITLE_INDEX);
            int startDay = mEventCursor.getInt(PROJECTION_START_DAY_INDEX);
            int startMinute = mEventCursor.getInt(PROJECTION_START_MINUTE_INDEX);
            int endMinute = mEventCursor.getInt(PROJECTION_END_MINUTE_INDEX);
            mNextEventLocation = mEventCursor.getString(PROJECTION_EVENT_LOCATION_INDEX);
            mNextEventStartTime = minutesToHours(startMinute) + ":" + minutesToMin(startMinute);
            mNextEventEndTime = minutesToHours(endMinute) + ":" + minutesToMin(endMinute);
            mNextEventDate = new JuilanDayConverter(startDay).getMonthDayString();
            hasNextEvent=true;
            Log.d(TAG, "Event Name: " + mNextEventTitle + " startDay : " + startDay + " startMinute: " + startMinute + "event time: " + mNextEventStartTime + "-" + mNextEventEndTime + "event date: "+mNextEventDate + " eventLocation: " + mNextEventLocation);
        }else {
            hasNextEvent=false;
            Log.d(TAG,"no upcomming events");
        }

        mEventCursor.close();
        return hasNextEvent;
    }

    private void initEventCursor(){
        ContentResolver contentResolver = App.getContext().getContentResolver();
        //set time frame of 24h for query
        Calendar rightNow = Calendar.getInstance();
        rightNow.set(rightNow.get(Calendar.YEAR), rightNow.get(Calendar.MONTH), rightNow.get(Calendar.DAY_OF_MONTH), rightNow.get(Calendar.HOUR_OF_DAY), rightNow.get(Calendar.MINUTE));

        long startMills = rightNow.getTimeInMillis();
        long oneDayInMillis = 86400000;
        long endMills = rightNow.getTimeInMillis() + oneDayInMillis;

        //add the start-end time to the Content URIs
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMills);
        ContentUris.appendId(builder, endMills);

        //define query params
        Uri uri = builder.build();
        String[] projection = new String[]{
                CalendarContract.Events.TITLE,              //0
                CalendarContract.Instances.START_DAY,       //1
                CalendarContract.Instances.START_MINUTE,    //2
                CalendarContract.Instances.END_MINUTE,      //3
                CalendarContract.Events.EVENT_LOCATION      //4
        };
        String selectionClause = CalendarContract.Instances.CALENDAR_ID + " = ?";
        String[] selectionArgs = new String[]{mCalendarID};
        String sortOrder = CalendarContract.Events.DTSTART + " ASC";

        //initialize cursor with query
        mEventCursor = contentResolver.query(uri, projection, selectionClause, selectionArgs, sortOrder);
    }


    public String getNextEventTitle() {
        return mNextEventTitle;
    }

    public String getNextEventLocation() {
        return mNextEventLocation;
    }

    public String getNextEventStartTime() {
        return mNextEventStartTime;
    }

    public String getNextEventEndTime() {
        return mNextEventEndTime;
    }

    public int minutesToHours(int allMinutes) {
        return allMinutes / 60;
    }

    public int minutesToMin(int allMinutes) {
        return allMinutes % 60;
    }

    public String getCalendarID() {
        return mCalendarID;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public String getOwnerName() {
        return mOwnerName;
    }


}

  /*  //TODO: remove next line after testing is done
    CalendarObject userCalendar = SettingsPersonalActivity.mSelectedCalendar;
        if (userCalendar.loadNextEvent()){
                Log.d(TAG, "Next event name: "+userCalendar.getDisplayName());
                }*/