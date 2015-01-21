package meet.you;

import java.util.Calendar;
import java.util.GregorianCalendar;

import meet.you.CalendarController.EventHandler;
import meet.you.CalendarController.EventInfo;
import meet.you.CalendarController.EventType;
import meet.you.data.MeetCursorLoader;
import meet.you.data.MeetData;
import meet.you.data.MeetData.Meet;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import meet.you.R;

public class MainActivity extends Activity implements EventHandler,
		OnClickListener, OnItemClickListener,
		LoaderManager.LoaderCallbacks<Cursor> {

	private CalendarController mController;
	Fragment mMonthFragment;
	Fragment mDayFragment;
	private EventInfo mEvent;
	private boolean mDayView;
	private long mTime;
	private long mEventID;
	boolean mEventView;

	// add by allen liu
	private Button mCreateBtn;
	private GridView mHourGrid;

	private final String TAG = "MA";

	private ListView mMeetList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

		new ImportEntries().execute(this);

		mController = CalendarController.getInstance(this);

		setContentView(R.layout.cal_layout);

		mMeetList = (ListView) findViewById(R.id.meet_list);

		mMonthFragment = new MonthByWeekFragment(System.currentTimeMillis(),
				false);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.cal_frame, mMonthFragment).commit();

		mController.registerEventHandler(R.id.cal_frame,
				(EventHandler) mMonthFragment);
		mController.registerFirstEventHandler(0, this);

		/*
		 * mHourGrid = (GridView) findViewById(R.id.hour_grid);
		 * 
		 * Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		 * mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		 * 
		 * List<ResolveInfo> apps =
		 * getPackageManager().queryIntentActivities(mainIntent, 0);
		 * 
		 * mHourGrid.setAdapter(new HourAdapter(apps));
		 * mHourGrid.setOnItemClickListener(this);
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.entry_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final int menuId = item.getItemId();
		switch (menuId) {
		case R.id.menu_create:
			Intent createMeet = new Intent();
			createMeet.setClass(this, EditMeetingActivity.class);
			startActivity(createMeet);

			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public long getSupportedEventTypes() {
		return EventType.GO_TO | EventType.VIEW_EVENT | EventType.UPDATE_TITLE;
	}

	@Override
	public void handleEvent(EventInfo event) {

		final int type = event.eventType;
		switch (type) {
		case EventType.GO_TO:
			/*
			 * mEvent = event; mDayView = true; FragmentTransaction ft =
			 * getFragmentManager().beginTransaction(); mDayFragment = new
			 * DayFragment(event.startTime.toMillis(true), 1);
			 * ft.replace(R.id.cal_frame,
			 * mDayFragment).addToBackStack(null).commit();
			 */

			long timeMillis = event.startTime.toMillis(true);
			Bundle bundle = new Bundle();
			bundle.putLong(EXTRA_FOCUS_DATE, timeMillis);
			
			getLoaderManager().restartLoader(LOADER_FOCUS_DAY_MEET, bundle, null);
			
			/*
			showFocusDateMeet(event.startTime);
			String timeLabel = DateUtils.formatDateTime(this, timeMillis,
					DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);

			Log.v(TAG, timeLabel);
			*/
			
			break;
		case EventType.VIEW_EVENT:
			mDayView = false;
			mEventView = true;
			this.mEvent = event;
			// FragmentTransaction ft = getFragmentManager().beginTransaction();
			// edit = new EditEvent(event.id);
			// ft.replace(R.id.cal_frame, edit).addToBackStack(null).commit();
			break;
		default:
			break;
		}

	}

	@Override
	public void eventsChanged() {

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		final int id = v.getId();
		/*
		 * switch(id){ case R.id.create: Intent createMeet = new Intent();
		 * createMeet.setClass(this, EditMeetingActivity.class);
		 * startActivity(createMeet); break; default: break; }
		 */
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO create new event with a specific time

	}

	private void showFocusDateMeet(Time focusTime) {

		GregorianCalendar focusDate = new GregorianCalendar(focusTime.year,
				focusTime.month, focusTime.monthDay);

		long dayStart = focusDate.getTimeInMillis();

		focusDate.roll(Calendar.DAY_OF_MONTH, true);

		long dayEnd = focusDate.getTimeInMillis();

		ContentResolver cr = getContentResolver();

		Cursor cs = cr
				.query(MeetData.URI,
						MeetListAdapter.DATA_COL,

						MeetData.KEY_WHEN + ">=? " + " AND "
								+ MeetData.KEY_WHEN + "< ?",

						new String[] { String.valueOf(dayStart),
								String.valueOf(dayEnd) }, null);

		if (cs != null) {

			final int N = cs.getCount();

			if (N > 0) {
				MeetListAdapter mla = new MeetListAdapter(this, cs, false);
				mMeetList.setAdapter(mla);
			}
			// for (int i = 0; i < N; i++) {
			// if (cs.moveToPosition(i)) {
			// Meet meet = new Meet();
			//
			// meet.topic = cs.getString(0);
			// meet.location = cs.getString(1);
			// meet.dateMillis = cs.getLong(2);
			// }
			// }

			// cs.close();

		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		MeetListAdapter mla = (MeetListAdapter) mMeetList.getAdapter();
		mla.changeCursor(null);
	}

	private int LOADER_FOCUS_DAY_MEET = 0;
	private String EXTRA_FOCUS_DATE = "focus_day";
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle data) {
		// TODO Auto-generated method stub
		switch (id) {
		case LOADER_FOCUS_DAY_MEET:
			long day = data.getLong(EXTRA_FOCUS_DATE);
			return new MeetCursorLoader(this,day,MeetListAdapter.DATA_COL);
			
			break;
		default:
			break;
		}
		
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cs) {
		// TODO Auto-generated method stub
		final int id = loader.getId();
		switch (id) {
		case LOADER_FOCUS_DAY_MEET:
			MeetListAdapter mla = (MeetListAdapter) mMeetList.getAdapter();
			mla.changeCursor(cs);
			break;
		default:
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
}
