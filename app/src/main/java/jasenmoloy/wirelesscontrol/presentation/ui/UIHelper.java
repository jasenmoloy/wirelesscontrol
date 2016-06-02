package jasenmoloy.wirelesscontrol.presentation.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.Toast;

import jasenmoloy.wirelesscontrol.R;

/**
 * Created by jasenmoloy on 5/20/16.
 */
public class UIHelper {

    /**
     * Visually and functionally enables a button
     * @param button        The button to enable
     */
    public static void enableButton(Button button) {
        button.setAlpha(1.0f);
        button.setClickable(true);
    }

    /**
     * Visually and functionally disables a button
     * @param button        The button to disable
     */
    public static void disableButton(Button button) {
        button.setAlpha(0.25f); //Make the button transparent to visually show the button is disabled.
        button.setClickable(false);
    }

    public static void displayToast(Context context, int duration, String text) {
        Toast.makeText(context, text, duration).show();
    }

    public static void displayOkDialog(Context context, int titleId,
                                       int messageId, int okTextId,
                                       boolean cancelable, DialogInterface.OnClickListener callback) {
        new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setMessage(messageId)
                .setCancelable(cancelable)
                .setPositiveButton(okTextId, callback)
                .show();
    }
}
