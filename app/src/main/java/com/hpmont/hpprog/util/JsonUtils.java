package com.hpmont.hpprog.util;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by 田富杰 on 2018/11/27.
 */
public class JsonUtils {
	public static String getJSONString(JSONObject o, String key) {
		try {
			return o.getString(key);
		} catch (Exception e) {
			Log.e("JsonUtils", e.toString());
		}
		return "";
	}
	
	public static JSONObject getJSONObject(JSONObject jo, String key){
		try {
			return jo.getJSONObject(key);
		} catch (Exception e) {
			Log.e("JsonUtils", e.toString());
		}
		return null;
	}


	public static JSONArray getJsonArray(JSONObject obj, String key) {
		try {
			return obj.getJSONArray(key);
		} catch (Exception e) {
			Log.e("JsonUtils", e.toString());
		}
		return null;
	}

	
	public static boolean getJSONBoolean(JSONObject jo, String key, boolean defalut){
		try {
			return jo.getBoolean(key);
		} catch (Exception e) {
			Log.e("JsonUtils", e.toString());
		}
		return defalut;
	}

	public static int getJSONInt(JSONObject o, String key) {
		try {
			return o.getInt(key);
		} catch (Exception e) {
			Log.e("JsonUtils", e.toString());
		}
		return -1;
	}
	
	public static boolean isJson(String text) {
		try {
			new JSONObject(text);
			return true;
		} catch (Exception e) {
			Log.e("JsonUtils", e.toString());
		}
		return false;
	}
	
	public static JSONObject getJSONObject(String text) {
		try {
			return new JSONObject(text);
		} catch (Exception e) {
			Log.e("JsonUtils", e.toString());
		}
		return null;
	}
}
