package com.example.calendarview;

import com.example.calendarview.CalendarController.EventHandler;
import com.example.calendarview.CalendarController.EventInfo;
import com.example.calendarview.CalendarController.EventType;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements EventHandler, OnClickListener {

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		
		new ImportEntries().execute(this);

		mController = CalendarController.getInstance(this);

		setContentView(R.layout.cal_layout);

		/*
		mCreateBtn = (Button) findViewById(R.id.create);
		mCreateBtn.setOnClickListener(this);
		*/
		
		mMonthFragment = new MonthByWeekFragment(System.currentTimeMillis(), false);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.cal_frame, mMonthFragment).commit();

		mController.registerEventHandler(R.id.cal_frame, (EventHandler) mMonthFragment);
		mController.registerFirstEventHandler(0, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final int menuId = item.getItemId();
		switch(menuId){
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

}
