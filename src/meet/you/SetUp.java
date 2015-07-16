package meet.you;

import java.util.Calendar;
import java.util.GregorianCalendar;

import meet.you.data.MeetData;

import org.json.JSONObject;

import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

public class SetUp extends Activity{
	
	private Switch floatWindow;
	private Button editLocation, shareQQ;
	private Tencent mTencent;
	private Switch edgeSwitch;
	
	private static final String APP_ID = "1104561257";
	
	public static String FloatWindow="floatWindow";
	private Boolean floatWhether;
	private boolean isEdgeEnable;
	
	SharedPreferences mySharedPreferencesTrue;
	SharedPreferences.Editor editorFloat;
	
	SharedPreferences mSharedPreferencesEdge;
	SharedPreferences.Editor editorFloatEdge;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup);
		
		ActionBar actionBar=getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setDisplayShowCustomEnabled(true); 
		LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		  View mView = inflator.inflate(R.layout.actionbar_setting, null);
		actionBar.setCustomView(mView);
		
		mView.findViewById(R.id.setFinishBtn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.setFinishBtn:
					SetUp.this.finish();
					break;

				default:
					break;
				}
			}
		});
		
		floatWindow = (Switch) findViewById(R.id.setup_switch);
		edgeSwitch=(Switch) findViewById(R.id.edgeSwith);
		editLocation = (Button) findViewById(R.id.setup_editLocation);
		shareQQ = (Button) findViewById(R.id.setup_qqshare);

		mTencent = Tencent.createInstance(APP_ID, this.getApplicationContext());
		
		mySharedPreferencesTrue = getSharedPreferences(FloatWindow,
				Activity.MODE_PRIVATE);
		mSharedPreferencesEdge=getSharedPreferences("isEdgeEnable",
				Activity.MODE_PRIVATE);
		editorFloat=mySharedPreferencesTrue.edit();
		editorFloatEdge=mSharedPreferencesEdge.edit();
		
		editorFloatEdge.putBoolean("isEdgeEnable", true);
		editorFloatEdge.commit();
		
		floatWhether = mySharedPreferencesTrue.getBoolean(FloatWindow, true);
		isEdgeEnable=mySharedPreferencesTrue.getBoolean("isEdgeEnable", true);
		
		floatWindow.setChecked(floatWhether);
		if (!floatWhether) {
			edgeSwitch.setEnabled(false);
		}
		edgeSwitch.setChecked(isEdgeEnable);
		
		edgeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					editorFloatEdge.putBoolean("isEdgeEnable", true);
					editorFloatEdge.commit();
					
				}else {
					editorFloatEdge.putBoolean("isEdgeEnable", false);
					editorFloatEdge.commit();
				}
			}
		});
		
		floatWindow.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
					if(isChecked){
						edgeSwitch.setEnabled(true);
						editorFloat.putBoolean(FloatWindow, true);
						editorFloat.commit();
						floatWindowService();
					}else{
						edgeSwitch.setEnabled(false);
						editorFloat.putBoolean(FloatWindow, false);
						editorFloat.commit();
						Intent intent=new Intent(SetUp.this, FxService.class);
						stopService(intent);
					}					
			}
		});

		editLocation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SetUp.this,
						EditLocationActivity.class);
				startActivity(intent);
			}
		});

		shareQQ.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mTencent.shareToQQ(SetUp.this, getShareBundle(),
						new BaseUiListener());
			}
		});
	}
	private Bundle getShareBundle() {
		Bundle bundle = new Bundle();
		bundle.putString("title", "开会啦");
		bundle.putString("imageUrl",
				"http://imgk.zol.com.cn/ask/100/99590_c6ccb1ec615ea4e7ad24d07c8f763453_s.jpg");
		bundle.putString("targetUrl", "http://www.gionee.com");
		bundle.putString("summary", "金立好");
		bundle.putString("site", "222222");
		bundle.putString("appName", "开会啦");
		return bundle;
	}
	private class BaseUiListener implements IUiListener {

		@Override
		public void onComplete(JSONObject response) {
			// mBaseMessageText.setText("onComplete:");
			// mMessageText.setText(response.toString());
			doComplete(response);
		}

		protected void doComplete(JSONObject values) {

		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(UiError arg0) {
			// TODO Auto-generated method stub

		}
	}
	public void floatWindowService() {

		Time today = new Time();
		today.setToNow();
		long time = System.currentTimeMillis();
		GregorianCalendar focusDate = new GregorianCalendar(today.year,
				today.month, today.monthDay);
		long dayStart = focusDate.getTimeInMillis();
		focusDate.roll(Calendar.DAY_OF_MONTH, true);

		long dayEnd = focusDate.getTimeInMillis();

		ContentResolver cr = getContentResolver();

		Cursor cs = cr.query(MeetData.URI, MeetListAdapter.DATA_COL,

		MeetData.KEY_WHEN + ">=? " + " AND " + MeetData.KEY_WHEN + "< ?",

		new String[] { String.valueOf(time), String.valueOf(dayEnd) }, null);

		if (cs != null) {

			final int N = cs.getCount();
			if (N > 0) {
				Intent intent = new Intent(SetUp.this, FxService.class);
				startService(intent);
			}else {
				Toast.makeText(getApplicationContext(),
						"今日无会议",
						Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

}
