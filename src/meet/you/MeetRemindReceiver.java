package meet.you;

import java.util.Date;

import meet.you.data.MeetData;
import meet.you.data.MeetData.Meet;

import meet.you.R;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MeetRemindReceiver extends BroadcastReceiver{
	private final String TAG = "MeetRec";
			
	public static String ACTION_REMIND = "meet.intent.action.REMIND";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		final String action = intent.getAction();
		Log.v(TAG,"action=" + action);
		
		if(ACTION_REMIND.equals(action)){
			handleRemindAction(context,intent);
		}
	}
	
	private void handleRemindAction(Context ctx, Intent intent){
		Uri uri = intent.getData();
		
		ContentResolver cr = ctx.getContentResolver();
		
		Cursor cs = cr.query(uri, new String []{
				MeetData.KEY_TOPIC,
				MeetData.KEY_WHERE,
				MeetData.KEY_WHEN
		}, null, null, null);
		
		if(cs !=null){
			if(cs.moveToFirst()){
				
				Meet meet = new Meet();
				
				meet.topic = cs.getString(0);
				meet.location = cs.getString(1);
				meet.dateMillis = cs.getLong(2);
				
				showRemind(ctx,meet);

			}
			
			cs.close();
		}
		
	}
	
	public static  void showRemind(Context ctx , Meet meet){
		View panel = View.inflate(ctx, R.layout.meet_remind_lyt,null);
		
		TextView tvTopic = (TextView) panel.findViewById(R.id.topic);
		TextView tvDate = (TextView) panel.findViewById(R.id.date);
		TextView tvLoc = (TextView) panel.findViewById(R.id.location);

		int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY;
		String time = DateUtils.formatDateTime(ctx,meet.dateMillis,flags);

		tvTopic.setText(meet.topic);
		tvDate.setText(time);
		tvLoc.setText(meet.location);

		AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
		adb.setTitle(R.string.meet_remind);
		adb.setView(panel);
		adb.setPositiveButton(R.string.remind_ok, null);
		adb.create().show();
	}

}
