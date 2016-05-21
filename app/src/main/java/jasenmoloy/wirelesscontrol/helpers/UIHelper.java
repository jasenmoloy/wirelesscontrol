package jasenmoloy.wirelesscontrol.helpers;

import android.widget.Button;

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
}
