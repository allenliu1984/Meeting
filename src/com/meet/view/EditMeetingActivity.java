package com.meet.view;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.xmlpull.v1.XmlSerializer;

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
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.meet.R;
import com.meet.data.MeetData;
import com.meet.data.MeetData.Meet;

public class EditMeetingActivity extends Activity implements OnClickListener {

	// add by allen liu
	private Button mCreateBtn;

	private EditText mTopic;
	private Spinner mLocation;

	private Button mDateSelBtn;
	private Button mTimeSelBtn;
	private Spinner mRemindSpin;

	private GregorianCalendar mDate;

	private final String TAG = "MeetEdit";

	private Meet mOrigMeet;
	
	private final String TAG_MEET = "meet";
	private final String ATTR_TOPIC = "topic";
	private final String ATTR_WHEN = "when";
	private final String ATTR_LOCATION = "location";
	private final String ATTR_PRE_TIME = "pretime";
	
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

//		mDateFormat = android.text.format.DateFormat.getDateFormat(this);
//		mTimeFormat = android.text.format.DateFormat.getTimeFormat(this);
		
		//Calendar cal = Calendar.getInstance();
		
		mDate = new GregorianCalendar();
		mDate.set(Calendar.MINUTE, 0);
		
		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY;
		String dateLabel = DateUtils.formatDateTime(this, mDate.getTimeInMillis(), flags);
		mDateSelBtn.setText(dateLabel);
		
		//incre one hour from now
		final int hour = mDate.get(Calendar.HOUR_OF_DAY);
		mDate.set(Calendar.HOUR_OF_DAY, hour + 1);
		String timeLabel =DateUtils.formatDateTime(this, mDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
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

		if (mOrigMeet != null) {
			menu.removeItem(R.id.menu_finish);
		} else {
			menu.removeItem(R.id.menu_share);
			menu.removeItem(R.id.menu_delete);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		Calendar cal = Calendar.getInstance();
		final int id = v.getId();

		switch (id) {
			case R.id.date:
				// TimeZone tz = TimeZone.getDefault();
				DatePickerDialog dpd = new DatePickerDialog(this, mDateSetLsn, mDate.get(Calendar.YEAR),
						mDate.get(Calendar.MONTH), mDate.get(Calendar.DAY_OF_MONTH));
				dpd.show();
				break;
			case R.id.time:
				TimePickerDialog tpd = new TimePickerDialog(this, mTimeSetLsn, mDate.get(Calendar.HOUR_OF_DAY),
						0, true);
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

			int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY;
			String dateLabel = DateUtils.formatDateTime(EditMeetingActivity.this, mDate.getTimeInMillis(), flags);
			mDateSelBtn.setText(dateLabel);
		}
	};

	private OnTimeSetListener mTimeSetLsn = new OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			mDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
			mDate.set(Calendar.MINUTE, minute);

			String dateLabel = DateUtils.formatDateTime(EditMeetingActivity.this, mDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
			mTimeSelBtn.setText(dateLabel);
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final int menuId = item.getItemId();
		switch (menuId) {
			case R.id.menu_finish:
				mOrigMeet = generateMeet();
				setupMeetRemind(mOrigMeet);
				invalidateOptionsMenu();
				break;
			case R.id.menu_share:
				
				shareMeet(mOrigMeet);
				break;
			case R.id.menu_delete:
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private static int[] PRE_REMIND_MINUTE = new int[] {15, 30};

	private Meet generateMeet() {

		Meet meet = new Meet();

		meet.topic = mTopic.getText().toString();
		if (TextUtils.isEmpty(meet.topic)) {
			meet.topic = mTopic.getHint().toString();
		}

		meet.location = (String) mLocation.getSelectedItem();
		meet.dateMillis = mDate.getTimeInMillis();
		meet.preMinute = PRE_REMIND_MINUTE[mRemindSpin.getSelectedItemPosition()];

		ContentValues cv = new ContentValues();

		cv.put(MeetData.KEY_TOPIC, meet.topic);
		cv.put(MeetData.KEY_WHEN, meet.dateMillis);
		cv.put(MeetData.KEY_WHERE, meet.location);
		cv.put(MeetData.KEY_PRE_TIME, meet.preMinute);

		ContentResolver cr = getContentResolver();

		meet.dbUri = cr.insert(MeetData.URI, cv);

		return meet;
	}

	private void setupMeetRemind(Meet meet) {

		if (meet == null) {
			Log.e(TAG, "meet can not be null");
			return;
		}

		long triggerTime = meet.dateMillis - meet.preMinute * 60 * 1000;

		Intent trigAction = new Intent();
		trigAction.setClass(this, MeetRemindReceiver.class);
		trigAction.setAction(MeetRemindReceiver.ACTION_REMIND);
		trigAction.setData(meet.dbUri);

		PendingIntent pi = PendingIntent.getBroadcast(this, 0, trigAction, PendingIntent.FLAG_ONE_SHOT);

		AlarmManager alm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alm.setExact(AlarmManager.RTC, triggerTime, pi);
	}

	
	private void shareMeet(Meet meet){
		if(meet == null){
			Log.e(TAG, "meet can not be null");
		}
		
		genMeetFile(meet);
	}
	
    String genMeetFile(Meet meet) {
        // Keep the old stopped packages around until we know the new ones have
        // been successfully written.
        File meetFile = new File("/sdcard/test.met");
        
        try {
        	meetFile.createNewFile();
        	
            final FileOutputStream fstr = new FileOutputStream(meetFile);
            final BufferedOutputStream str = new BufferedOutputStream(fstr);

            final XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(str, "utf-8");
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

			serializer.startTag(null, TAG_MEET);
			
			serializer.attribute(null, ATTR_TOPIC, meet.topic);
			serializer.attribute(null, ATTR_LOCATION, meet.location);
			serializer.attribute(null, ATTR_PRE_TIME, String.valueOf(meet.preMinute));
			serializer.attribute(null, ATTR_WHEN, String.valueOf(meet.dateMillis));
			
			serializer.endTag(null, TAG_MEET);

            serializer.endDocument();

            str.flush();
            fstr.getFD().sync();
            
            fstr.close();
            str.close();

            // New settings successfully written, old ones are no longer
            // needed.
            return "";
        } catch(java.io.IOException e) {
            Log.e(TAG, "generate meet file failed !!!", e);
        }

        
        return "";
    }


}
