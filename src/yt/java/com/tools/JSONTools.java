package yt.java.com.tools;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JSONTools {

	public static int HasKeyValue(JSONArray list, String key, String value) {
		for (int i = 0; i < list.size(); i++) {
			JSONObject temp = list.getJSONObject(i);
			String tempValue = temp.getString(key);
			if(tempValue.equals(value) == true) {
				return i;
			}
		}
		
		return -1;
	}

}
