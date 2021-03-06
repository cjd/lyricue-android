/*
 * This file is part of Lyricue.
 *
 *     Lyricue is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lyricue.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lyricue.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class LyricueDisplay extends Service {

    private HostItem[] hosts = null;

    public LyricueDisplay() {
        hosts = new HostItem[1];
        hosts[0].hostname = "#demo";
        hosts[0].port = 0;
    }

    public LyricueDisplay(String host, int port) {
        hosts = new HostItem[1];
        hosts[0].hostname = host;
        hosts[0].port = port;
    }

    public LyricueDisplay(HostItem[] hosts) {
        this.hosts = hosts;
    }

    public LyricueDisplay(HostItem host) {
        hosts = new HostItem[1];
        hosts[0] = host;
    }

    void logError(String error_text) {
        String TAG = "Lyricue";
        Log.d(TAG, error_text);
    }

    public void runCommand_noreturn(final String command, final String option1,
                                    final String option2) {
        if (hosts != null) {
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
    }

    /*public boolean checkRunning() {
        if (hosts.length == 0) {
            return true;
        }

        try {
            Socket sc = new Socket();
            InetSocketAddress hostaddr = new InetSocketAddress(hosts[0].hostname, hosts[0].port);
            sc.connect(hostaddr, 5000);
            sc.close();
            return true;
        } catch (UnknownHostException e) {
            logError("Don't know about host: " + hosts[0].hostname + ":" + hosts[0].port);
            return false;
        } catch (IOException e) {
            logError("Couldn't get I/O socket for the connection to: "
                    + hosts[0].hostname + ":" + hosts[0].port);
            return false;
        }
    }*/

    public String runCommand(Integer hostnum, String command, String option1,
                             String option2) {
        String result = "";
        if ((hosts == null) || (hosts.length == 0) || (hosts[hostnum] == null) || (hosts[hostnum].hostname.equals("#demo"))) {
            return result;
        }
        Socket sc = null;
        DataOutputStream os = null;
        HostItem host = hosts[hostnum];
        try {
            sc = new Socket(host.hostname, host.port);
        } catch (UnknownHostException e) {
            logError("Don't know about host: " + host.hostname);
        } catch (IOException e) {
            logError("Couldn't get I/O socket for the connection to: "
                    + host.hostname);
        }
        if (sc != null) {
            try {
                os = new DataOutputStream(sc.getOutputStream());
            } catch (UnknownHostException e) {
                logError("Don't know about host: " + host.hostname);
            } catch (IOException e) {
                logError("Couldn't get I/O output for the connection to: "
                        + host.hostname);
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
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                is.close();
                result = sb.toString();
                try {
                    os.close();
                    sc.close();
                } catch (IOException ignored) {
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
            //noinspection ConstantConditions,ConstantConditions
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
        if (result.equals("")) {
            return null;
        } else {
            try {
                JSONObject json = new JSONObject(result);
                return json.getJSONArray("results");
            } catch (JSONException e) {
                logError("Error parsing data " + e.toString());
                return null;
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
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

    @SuppressWarnings("SameParameterValue")
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

}
