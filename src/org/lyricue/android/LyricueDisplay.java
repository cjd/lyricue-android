package org.lyricue.android;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class LyricueDisplay extends Service {
	private static final String TAG = Lyricue.class.getSimpleName();
	Socket sc = null;
	DataOutputStream os = null;
	public String hostip = "";
	public Context context = null;

	public LyricueDisplay(String hostip) {
		this.hostip = hostip;
	}

	public void logError(String error_text) {
		Log.d(TAG, error_text);
	}

	public void runCommand_noreturn(final String command, final String option1,
			final String option2) {
		new Thread(new Runnable() {
			public void run() {
				LyricueDisplay ld = new LyricueDisplay(hostip);
				ld.runCommand(command, option1, option2);
			}
		}).start();
	}
	
	public boolean checkRunning() {
		if (hostip.equals("#demo")) {
			return true;
		}
		try {
			sc = new Socket();
			InetSocketAddress hostaddr = new InetSocketAddress(hostip,2346); 
			sc.connect(hostaddr, 10000);
		} catch (UnknownHostException e){
			logError("Don't know about host: " + hostip);
			return false;
		} catch (IOException e){
			logError("Couldn't get I/O socket for the connection to: "
					+ hostip);
			return false;
		}
		return true;
	}

	public String runCommand(String command, String option1, String option2) {
		String result = "";
		if (hostip.equals("demo")) {
			return result;
		}
		if (sc == null) {
			try {
				sc = new Socket(hostip, 2346);
			} catch (UnknownHostException e) {
				logError("Don't know about host: " + hostip);
			} catch (IOException e) {
				logError("Couldn't get I/O socket for the connection to: "
						+ hostip);
			}
		}
		if (sc != null && os == null) {
			try {
				os = new DataOutputStream(sc.getOutputStream());
			} catch (UnknownHostException e) {
				logError("Don't know about host: " + hostip);
			} catch (IOException e) {
				logError("Couldn't get I/O output for the connection to: "
						+ hostip);
			}
		}
		if (sc != null && os != null) {
			try {
				option1=option1.replace("\n", "#BREAK#").replace(":", "#SEMI#");
				option2=option2.replace("\n", "#BREAK#").replace(":", "#SEMI#");
				os.writeBytes(command + ":" + option1 + ":" + option2 + "\n");
				os.flush();
				InputStream is = sc.getInputStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "utf-8"), 128);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
				try {
					os.close();
					sc.close();
					os = null;
					sc = null;
				} catch (IOException f) {
				}
			} catch (UnknownHostException e) {
				logError("Trying to connect to unknown host: " + e);
			} catch (IOException e) {
				logError("IOException:  " + e);
			}
		} else {
			if (sc != null) {
				try {
					sc.close();
					sc = null;
				} catch (IOException e) {
					logError("IOException:  " + e);
				}
			}
			if (os != null) {
				try {
					os.close();
					os = null;
				} catch (IOException e) {
					logError("IOException:  " + e);
				}
			}
		}
		return result;
	}

	public JSONArray runQuery(String database, String query) {
		String result = runCommand("query", database, query);
		if (result == "") {
			return null;
		} else {
			try {
				JSONObject json = new JSONObject(result);
				JSONArray jArray = json.getJSONArray("results");
				return jArray;
			} catch (JSONException e) {
				logError("Error parsing data " + e.toString());
				return null;
			}
		}
	}

	public String runQuery_string(String database, String query, String retval) {
		JSONArray jArray = runQuery(database, query);
		if (jArray == null) {
			return null;
		}
		String retstring = "";
		try {
			retstring = jArray.getJSONObject(0).getString(retval);
		} catch (JSONException e) {
			logError("Error parsing data " + e.toString());
		}
		return retstring;
	}

	public int runQuery_int(String database, String query, String retval) {
		JSONArray jArray = runQuery(database, query);
		if (jArray == null) {
			return -1;
		}
		int retstring = -1;
		try {
			retstring = jArray.getJSONObject(0).getInt(retval);
		} catch (JSONException e) {
			logError("Error parsing data " + e.toString());
		}
		return retstring;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
