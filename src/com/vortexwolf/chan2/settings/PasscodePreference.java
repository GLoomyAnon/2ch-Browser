package com.vortexwolf.chan2.settings;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.vortexwolf.chan2.R;
import com.vortexwolf.chan2.asynctasks.CheckPasscodeTask;
import com.vortexwolf.chan2.common.Websites;
import com.vortexwolf.chan2.common.utils.AppearanceUtils;
import com.vortexwolf.chan2.common.utils.StringUtils;
import com.vortexwolf.chan2.interfaces.ICheckPasscodeView;

public class PasscodePreference extends EditTextPreference {

    public PasscodePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // check the entered passcode
        if (positiveResult) {
            String passcode = this.getText();
            this.sendPasscodeToServer(passcode);
        }
    }

    public void sendPasscodeToServer(String passcode) {
        CheckPasscodeTask task = new CheckPasscodeTask(Websites.getDefault(), new CheckPasscodeView(), passcode);
        task.execute();
    }

    private class CheckPasscodeView implements ICheckPasscodeView {
        @Override
        public void onPasscodeRemoved() {
            // show nothing
        }

        @Override
        public void onPasscodeChecked(boolean isSuccess, String errorMessage) {
            Context context = PasscodePreference.this.getContext();
            if (isSuccess) {
                AppearanceUtils.showToastMessage(context, context.getString(R.string.notification_passcode_correct));
            } else {
                String error = !StringUtils.isEmpty(errorMessage) ? errorMessage : context.getString(R.string.notification_passcode_incorrect);
                AppearanceUtils.showToastMessage(context, error);
            }
        }
    }
}
