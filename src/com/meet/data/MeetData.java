package com.meet.data;

import android.net.Uri;

public interface MeetData {
	
	public static Uri URI = MeetProvider.CONTENT_URI;
	
	public static String KEY_TOPIC = MeetProvider.KEY_TOPIC;
	public static String KEY_WHEN = MeetProvider.KEY_DATE;
	public static String KEY_WHERE = MeetProvider.KEY_WHERE;
	public static String KEY_PRE_TIME = MeetProvider.KEY_PRE_TIME;
	
	
	public class Meet{
		public String topic;
		public String location;
		public long dateMillis;
		public int  preMinute;
	}
	
}
