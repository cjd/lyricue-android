package org.lyricue.android;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

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

	public String[] hosts = null;
	public Context context = null;

	public LyricueDisplay(Map<String, String> hostmap, String profile) {
		hosts = new String[hostmap.size()];
		int i = 0;
		for (Map.Entry<String, String> entry : hostmap.entrySet()) {
			if (entry.getValue().equals(profile) || profile.isEmpty()) {
				hosts[i] = entry.getKey();
				i++;
			}
		}
	}
	
	public LyricueDisplay(String host) {
		hosts = new String[1];
		hosts[0] = host;
	}
	public LyricueDisplay(String[] hosts) {
		this.hosts = hosts;
	}

	public void logError(String error_text) {
		Log.d(TAG, error_text);
	}

	public void runCommand_noreturn(final String command, final String option1,
			final String option2) {
		new Thread(new Runnable() {
			public void run() {
				LyricueDisplay ld = new LyricueDisplay(hosts);
				for (int i = 0; i < hosts.length; i++) {
					if (hosts[i] != null) {
						ld.runCommand(i, command, option1, option2);
					}
				}
			}
		}).start();
	}

	public boolean checkRunning() {
		if (hosts.length == 0) {
			return true;
		}

		try {
			Socket sc = new Socket();
			String[] host = hosts[0].split(":");
			InetSocketAddress hostaddr = new InetSocketAddress(host[0],
					Integer.parseInt(host[1]));
			sc.connect(hostaddr, 5000);
			sc.close();
			return true;
		} catch (UnknownHostException e) {
			logError("Don't know about host: " + hosts[0]);
			return false;
		} catch (IOException e) {
			logError("Couldn't get I/O socket for the connection to: "
					+ hosts[0]);
			return false;
		}
	}

	public String runCommand(Integer hostnum, String command, String option1,
			String option2) {
		String result = "";
		if ((hosts == null) || (hosts.length == 0) || (hosts[hostnum] == null)) {
			return result;
		}
		Socket sc = null;
		DataOutputStream os = null;
		String[] host = hosts[hostnum].split(":");
		try {
			sc = new Socket(host[0], Integer.parseInt(host[1]));
		} catch (UnknownHostException e) {
			logError("Don't know about host: " + hosts[hostnum]);
		} catch (IOException e) {
			logError("Couldn't get I/O socket for the connection to: "
					+ hosts[hostnum]);
		}
		if (sc != null) {
			try {
				os = new DataOutputStream(sc.getOutputStream());
			} catch (UnknownHostException e) {
				logError("Don't know about host: " + hosts[hostnum]);
			} catch (IOException e) {
				logError("Couldn't get I/O output for the connection to: "
						+ hosts[hostnum]);
			}
		}
		if (sc != null && os != null) {
			try {
				option1 = option1.replace("\n", "#BREAK#").replace(":",
						"#SEMI#");
				option2 = option2.replace("\n", "#BREAK#").replace(":",
						"#SEMI#");
				os.writeBytes(command + ":" + option1 + ":" + option2 + "\n");
				os.flush();
				InputStream is = sc.getInputStream();
				StringBuilder sb = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "utf-8"), 128);
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
				try {
					os.close();
					sc.close();
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
				} catch (IOException e) {
					logError("IOException:  " + e);
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					logError("IOException:  " + e);
				}
			}
		}
		return result;
	}

	public JSONArray runQuery(String database, String query) {
		String result = runCommand(0, "query", database, query);
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
