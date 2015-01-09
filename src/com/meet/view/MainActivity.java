package com.meet.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;

import com.meet.R;
import com.meet.view.CalendarController.EventHandler;
import com.meet.view.CalendarController.EventInfo;
import com.meet.view.CalendarController.EventType;

public class MainActivity extends Activity implements EventHandler, OnClickListener, OnItemClickListener {

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

		new ImportEntries().execute(this);

		mController = CalendarController.getInstance(this);

		setContentView(R.layout.cal_layout);

		mMonthFragment = new MonthByWeekFragment(System.currentTimeMillis(), false);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.cal_frame, mMonthFragment).commit();

		mController.registerEventHandler(R.id.cal_frame, (EventHandler) mMonthFragment);
		mController.registerFirstEventHandler(0, this);

		/*
		mHourGrid = (GridView) findViewById(R.id.hour_grid);

		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		List<ResolveInfo> apps = getPackageManager().queryIntentActivities(mainIntent, 0);

		mHourGrid.setAdapter(new HourAdapter(apps));
		mHourGrid.setOnItemClickListener(this);
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
				this.mEvent = event;
				mDayView = true;
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				mDayFragment = new DayFragment(event.startTime.toMillis(true), 1);
				ft.replace(R.id.cal_frame, mDayFragment).addToBackStack(null).commit();
				break;
			case EventType.VIEW_EVENT:
				mDayView = false;
				mEventView = true;
				this.mEvent = event;
//						FragmentTransaction ft = getFragmentManager().beginTransaction();
//						edit = new EditEvent(event.id);
//						ft.replace(R.id.cal_frame, edit).addToBackStack(null).commit();
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
		switch(id){
			case R.id.create:
				Intent createMeet = new Intent();
				createMeet.setClass(this, EditMeetingActivity.class);
				startActivity(createMeet);
				break;
			default:
				break;
		}
		*/
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO create new event with a specific time
		
	}

}
