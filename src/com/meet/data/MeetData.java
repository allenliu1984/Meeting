package com.meet.data;

import android.net.Uri;

public interface MeetData {
	
	public static Uri URI = MeetProvider.CONTENT_URI;
	
	public static String KEY_TOPIC = MeetProvider.TOPIC;
	public static String KEY_WHEN = MeetProvider.WHEN;
	public static String KEY_WHERE = MeetProvider.WHERE;
	public static String KEY_PRE_TIME = MeetProvider.PRE_TIME;
	
	
}
