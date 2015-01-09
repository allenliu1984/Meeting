package com.meet.view;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.meet.R;
import com.meet.data.MeetData;

public class EditMeetingActivity extends Activity implements OnClickListener {

	// add by allen liu
	private Button mCreateBtn;

	private EditText mTopic;
	private Spinner mLocation;

	private Button mDateSelBtn;
	private Button mTimeSelBtn;
	private Spinner mRemindSpin;
	
	private boolean mEditFinished = false;

	private DateFormat mDateFormat;
	private DateFormat mTimeFormat;
	
	
	private GregorianCalendar mDate;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.new_meet);

		mTopic = (EditText) findViewById(R.id.topic);
		mTopic.selectAll();
		
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.viewClicked(mTopic);
		
		mLocation = (Spinner) findViewById(R.id.location);
		mLocation.setSelection(0);
		
		mDateSelBtn = (Button) findViewById(R.id.date);
		mDateSelBtn.setOnClickListener(this);
		
		mTimeSelBtn = (Button) findViewById(R.id.time);
		mTimeSelBtn.setOnClickListener(this);
		
		mRemindSpin = (Spinner) findViewById(R.id.remind_time);
		mRemindSpin.setSelection(0);

		mDateFormat = android.text.format.DateFormat.getDateFormat(this);
		mTimeFormat = android.text.format.DateFormat.getTimeFormat(this);
		
		Calendar cal = Calendar.getInstance();
		
		mDate = new GregorianCalendar();
		mDate.set(Calendar.MINUTE, 0);
		
		String dateLabel = mDateFormat.format(mDate.getTime());
		mDateSelBtn.setText(dateLabel);
		
		String timeLabel = mTimeFormat.format(mDate.getTime());
		mTimeSelBtn.setText(timeLabel);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
		if(mEditFinished){
			menu.removeItem(R.id.menu_finish);
		} else{
			menu.removeItem(R.id.menu_share);
			menu.removeItem(R.id.menu_delete);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		Calendar cal = Calendar.getInstance();
		final int id = v.getId();
		
		switch(id){
			case R.id.date:
    			//TimeZone tz = TimeZone.getDefault();
				DatePickerDialog dpd = new DatePickerDialog(this, mDateSetLsn, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
				dpd.show();
				break;
			case R.id.time:
				TimePickerDialog tpd = new TimePickerDialog(this,mTimeSetLsn , cal.get(Calendar.HOUR_OF_DAY), 0, true);
				tpd.show();
				break;
			default:
				break;
		}
		
	}

	
	private OnDateSetListener mDateSetLsn = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			// TODO Auto-generated method stub
			mDate.set(year, monthOfYear, dayOfMonth);
			
			String dateLabel = mDateFormat.format(mDate.getTime());
			mDateSelBtn.setText(dateLabel);
		}
	};

	private OnTimeSetListener mTimeSetLsn = new OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			mDate.set(Calendar.HOUR_OF_DAY,hourOfDay);
			mDate.set(Calendar.MINUTE,minute);
			
			String dateLabel = mTimeFormat.format(mDate.getTime());
			mTimeSelBtn.setText(dateLabel);
		}
	};

	private void setDate(TextView view, long millis) {
		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY
				| DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY;

		// Unfortunately, DateUtils doesn't support a timezone other than the
		// default timezone provided by the system, so we have this ugly hack
		// here to trick it into formatting our time correctly. In order to
		// prevent all sorts of craziness, we synchronize on the TimeZone class
		// to prevent other threads from reading an incorrect timezone from
		// calls to TimeZone#getDefault()
		// TODO fix this if/when DateUtils allows for passing in a timezone
		String dateString = "";
		synchronized (TimeZone.class) {
			/*
			TimeZone.setDefault(TimeZone.getTimeZone(mTimezone));
			dateString = DateUtils.formatDateTime(mActivity, millis, flags);
			// setting the default back to null restores the correct behavior
			TimeZone.setDefault(null);
			*/
		}
		view.setText(dateString);
	}

	private void setTime(TextView view, long millis) {
		/*
		int flags = DateUtils.FORMAT_SHOW_TIME;
		flags |= DateUtils.FORMAT_CAP_NOON_MIDNIGHT;
		if (DateFormat.is24HourFormat(mActivity)) {
		    flags |= DateUtils.FORMAT_24HOUR;
		}
		*/
		// Unfortunately, DateUtils doesn't support a timezone other than the
		// default timezone provided by the system, so we have this ugly hack
		// here to trick it into formatting our time correctly. In order to
		// prevent all sorts of craziness, we synchronize on the TimeZone class
		// to prevent other threads from reading an incorrect timezone from
		// calls to TimeZone#getDefault()
		// TODO fix this if/when DateUtils allows for passing in a timezone
		String timeString = "";
		synchronized (TimeZone.class) {
			/*
			TimeZone.setDefault(TimeZone.getTimeZone(mTimezone));
			timeString = DateUtils.formatDateTime(mActivity, millis, flags);
			TimeZone.setDefault(null);
			*/
		}
		view.setText(timeString);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final int menuId = item.getItemId();
		switch (menuId) {
			case R.id.menu_finish:
				Meet meet = generateMeet();
				setupMeet(meet);
				
				invalidateOptionsMenu();
				break;
			case R.id.menu_share:
				break;
			case R.id.menu_delete:
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private static int [] PRE_REMIND_MINUTE = new int []{
		15,
		30
	};
	
	private void setMeetAlarm(Meet meet){
		
		
	}
	
	private Meet generateMeet(){
		
		Meet meet = new Meet();
		
		meet.topic = mTopic.getText().toString();
		if(TextUtils.isEmpty(meet.topic)){
			meet.topic = mTopic.getHint().toString();
		}
		
		meet.location= (String)mLocation.getSelectedItem();
		meet.dateMillis = mDate.getTimeInMillis();
		meet.preMinute = PRE_REMIND_MINUTE[mRemindSpin.getSelectedItemPosition()];
		
		return meet;
	}
	
	private void setupMeet(Meet meet){

		if(meet == null){
			return;
		}
		
		Uri uri = writeDb(meet);
		
		long triggerTime = meet.dateMillis - meet.preMinute*60*1000;
		
		Intent trigAction = new Intent();
		trigAction.setClass(this, MeetRemindReceiver.class);
		trigAction.setData(uri);
		
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, trigAction, PendingIntent.FLAG_ONE_SHOT);
		
		AlarmManager alm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alm.setExact(AlarmManager.RTC,triggerTime , pi);
	}
	
	
	private Uri writeDb(Meet meet){
		ContentValues cv = new ContentValues();
		
		cv.put(MeetData.KEY_TOPIC, meet.topic);
		cv.put(MeetData.KEY_WHEN, meet.dateMillis);
		cv.put(MeetData.KEY_WHERE, meet.location);
		cv.put(MeetData.KEY_PRE_TIME, meet.preMinute);
		
		ContentResolver cr = getContentResolver();
		
		return cr.insert(MeetData.URI, cv);
	}
	private class Meet{
		String topic;
		String location;
		long dateMillis;
		int  preMinute;
	}
}
