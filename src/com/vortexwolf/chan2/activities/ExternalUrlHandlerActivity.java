package com.vortexwolf.chan2.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.vortexwolf.chan2.common.Factory;
import com.vortexwolf.chan2.services.NavigationService;

public class ExternalUrlHandlerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NavigationService navigationService = Factory.resolve(NavigationService.class);
        navigationService.navigate(this.getIntent().getData(), this, Intent.FLAG_ACTIVITY_NEW_TASK, false);

        this.finish();
    }
}
