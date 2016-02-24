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
        Log.d(tag, msg);
    }

    /**
     * Write warning text out to Logcat.
     */
    public static void LogWarn(String tag, String msg) {
        Log.w(tag, msg);
    }

    /**
     * Write warning text out to Logcat.
     */
    public static void LogError(String tag, String msg) {
        Log.e(tag, msg);
    }

    /**
     * Write warning text out to Logcat.
     */
    public static void LogVerbose(String tag, String msg) {
        Log.v(tag, msg);
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
