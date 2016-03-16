package jasenmoloy.wirelesscontrol.debug;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Created by jasenmoloy on 2/23/16.
 */
public class Debug {

    /**
     * Write debug text out to Logcat.
     */
    public static void LogDebug(String tag, String msg) {
        if(msg != null)
            Log.d(tag, msg);
        else
            Log.d(tag, "null");
    }

    /**
     * Write warning text out to Logcat.
     */
    public static void LogWarn(String tag, String msg) {
        if(msg != null)
            Log.w(tag, msg);
        else
            Log.w(tag, "null");
    }

    /**
     * Write error text out to Logcat.
     */
    public static void LogError(String tag, String msg) {
        if(msg != null)
            Log.e(tag, msg);
        else
            Log.e(tag, "null");
    }

    /**
     * Write verbose text out to Logcat.
     */
    public static void LogVerbose(String tag, String msg) {
        if(msg != null)
            Log.v(tag, msg);
        else
            Log.v(tag, "null");
    }

    /**
     * Show a basic debug dialog to provide more info on the built-in debug
     * options.
     */
    public static void ShowDebugOkDialog(Context context, String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(body)
                .setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }
    public static void ShowDebugOkDialog(Context context, int titleResourceId, String bodyResourceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(titleResourceId)
                .setMessage(bodyResourceId)
                .setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }
}
