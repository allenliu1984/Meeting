package meet.you;

import java.util.Calendar;
import java.util.GregorianCalendar;

import meet.you.data.Constants;
import meet.you.data.MeetData;
import meet.you.data.MeetUtil;
import meet.you.data.Util;
import meet.you.data.MeetData.Meet;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TimePicker;
import meet.you.R;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXFileObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;

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

	private IWXAPI mWXapi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.new_meet);

		mWXapi = WXAPIFactory.createWXAPI(this, Constants.APP_ID);

		mTopic = (EditText) findViewById(R.id.topic);
		mTopic.selectAll();

		mLocation = (Spinner) findViewById(R.id.location);
		mLocation.setSelection(0);

		mDateSelBtn = (Button) findViewById(R.id.date);
		mDateSelBtn.setOnClickListener(this);

		mTimeSelBtn = (Button) findViewById(R.id.time);
		mTimeSelBtn.setOnClickListener(this);

		mRemindSpin = (Spinner) findViewById(R.id.remind_time);
		mRemindSpin.setSelection(0);

		String action = getIntent().getAction();
		if(MeetData.ACTION_MEET_VIEW.equals(action)){
			//may be we should check pre-remind minute,any way,no one cares,set it as default
			Uri data = getIntent().getData();
			initByMeetRecord(data);
		} else {
			Uri data = getIntent().getData();
			if (data != null) {
				initByMeetFile(data);
			} else {
				initDefault();
			}
		}
	}

	private void initByMeetRecord(Uri uri) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * if has data,it may be in edit or view mode
	 * @param data
	 */
	private void initByMeetFile(Uri data) {
		Meet meetReq = MeetUtil.readMeetFromFile(data.getPath());

		if (meetReq == null) {
			showFailDlg();
			return;
		}

		mTopic.setText(meetReq.topic);
		mTopic.setEnabled(false);

		mDate = new GregorianCalendar();
		mDate.setTimeInMillis(meetReq.dateMillis);

		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY;
		String dateLabel = DateUtils.formatDateTime(this, mDate.getTimeInMillis(), flags);
		mDateSelBtn.setText(dateLabel);
		mDateSelBtn.setEnabled(false);

		// incre one hour from now
		final int hour = mDate.get(Calendar.HOUR_OF_DAY);
		mDate.set(Calendar.HOUR_OF_DAY, hour + 1);
		String timeLabel = DateUtils
				.formatDateTime(this, mDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
		mTimeSelBtn.setText(timeLabel);
		mTimeSelBtn.setEnabled(false);

		int pos = findLocation(meetReq.location);
		mLocation.setSelection(pos);
		mLocation.setEnabled(false);
		
		setTitle(R.string.meeting_request);
	}

	private void showFailDlg() {
		// TODO Auto-generated method stub
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setMessage(R.string.meet_req_failed);
		adb.setPositiveButton(R.string.remind_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	private void initDefault() {
		
		long now = getIntent().getLongExtra(MeetData.EXTRA_FOCUS_DATE, System.currentTimeMillis());
		
		mDate = new GregorianCalendar();
		mDate.setTimeInMillis(now);
		mDate.set(Calendar.MINUTE, 0);

		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY;
		String dateLabel = DateUtils.formatDateTime(this, mDate.getTimeInMillis(), flags);
		mDateSelBtn.setText(dateLabel);

		// incre one hour from now
		final int hour = mDate.get(Calendar.HOUR_OF_DAY);
		mDate.set(Calendar.HOUR_OF_DAY, hour + 1);
		String timeLabel = DateUtils
				.formatDateTime(this, mDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
		mTimeSelBtn.setText(timeLabel);
		
		setTitle(R.string.meeting_create);
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
				TimePickerDialog tpd = new TimePickerDialog(this, mTimeSetLsn,
						mDate.get(Calendar.HOUR_OF_DAY), 0, true);
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
			String dateLabel = DateUtils.formatDateTime(EditMeetingActivity.this, mDate.getTimeInMillis(),
					flags);
			mDateSelBtn.setText(dateLabel);
		}
	};

	private OnTimeSetListener mTimeSetLsn = new OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			mDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
			mDate.set(Calendar.MINUTE, minute);

			String dateLabel = DateUtils.formatDateTime(EditMeetingActivity.this, mDate.getTimeInMillis(),
					DateUtils.FORMAT_SHOW_TIME);
			mTimeSelBtn.setText(dateLabel);
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final int menuId = item.getItemId();
		switch (menuId) {
			case R.id.menu_finish:
				freezeWidget();
				
				mOrigMeet = generateMeet();
				setupMeetRemind(mOrigMeet);
				invalidateOptionsMenu();
				break;
			case R.id.menu_share:
				
				sendToWeixin(mOrigMeet);

				break;
			case R.id.menu_delete:
				deleteMeet();
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void deleteMeet() {
		// TODO Auto-generated method stub
		ContentResolver cr = getContentResolver();
		cr.delete(mOrigMeet.dbUri, null, null);
		
		finish();
	}

	private void freezeWidget() {
		// TODO Auto-generated method stub
		mTopic.setEnabled(false);
		mLocation.setEnabled(false);
		mDateSelBtn.setEnabled(false);
		mTimeSelBtn.setEnabled(false);
		mRemindSpin.setEnabled(false);
	}

	private void sendToWeixin(Meet meet) {
		
		String path = MeetUtil.genMeetFile(meet);
		
		Log.v(TAG, "file path=" + path);
		final WXFileObject appdata = new WXFileObject();

		appdata.filePath = path;
		appdata.fileData = Util.readFromFile(path, 0, -1);
		
		final WXMediaMessage msg = new WXMediaMessage();
		
		Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
		msg.setThumbImage(icon);
		
		String time = 	DateUtils.formatDateTime(EditMeetingActivity.this, meet.dateMillis,DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE);
		msg.title = meet.topic + "\n" + time + " " + meet.location;
		
		msg.description = getResources().getString(R.string.meeting_request);
		msg.mediaObject = appdata;

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = Util.buildTransaction("appdata");
		req.message = msg;
		req.scene = /*isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : */SendMessageToWX.Req.WXSceneSession;

		mWXapi.sendReq(req);
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
		alm.set(AlarmManager.RTC, triggerTime, pi);
	}

	
	public int findLocation(String location){
		 String [] addrs = getResources().getStringArray(R.array.meeting_rooms);
		 
		 for(int i=0; i<addrs.length; ++i){
			 if(addrs[i].equals(location)){
				 return i;
			 }
		 }
		 return 0;
	}
	
}
