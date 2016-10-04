/**
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazon.android.ui.activities;

import com.amazon.android.configuration.ConfigurationManager;
import com.amazon.android.ui.constants.ConfigurationConstants;
import com.amazon.utils.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * An activity that handles UI for second screen authentication. To use this class, implement the
 * abstract methods: {@link #getRegistrationCode()} and {@link #getAuthenticationToken()}.
 */
public abstract class SecondScreenAuthenticationActivity extends Activity {

    /**
     * Debug tag.
     */
    private static final String TAG = SecondScreenAuthenticationActivity.class.getName();

    /**
     * This text view holds the registration code that the user must enter on the 2nd screen.
     */
    private TextView mRegistrationCode;

    /**
     * Button that triggers the call to get the authentication token. To be clicked after the user
     * has entered the registration code on the 2nd screen.
     */
    private Button mSubmitButton;

    /**
     * Button that triggers the call to get a new registration code.
     */
    private Button mGetCodeButton;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_screen_auth);

        mSubmitButton = (Button) findViewById(R.id.btn_submit);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getAuthenticationToken();
            }
        });

        mGetCodeButton = (Button) findViewById(R.id.btn_get_code);
        mGetCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getRegistrationCode();
            }
        });

        mRegistrationCode = (TextView) findViewById(R.id.login_instruction_reg_code);

        CalligraphyUtils.applyFontToTextView(this, mRegistrationCode, ConfigurationManager
                .getInstance(this).getTypefacePath(ConfigurationConstants.BOLD_FONT));

        getRegistrationCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {

        super.onResume();
        mSubmitButton.setEnabled(true);
        mGetCodeButton.setEnabled(true);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    protected void attachBaseContext(Context newBase) {
        // This lets us get global font support.
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * Get the "Get Code" button.
     *
     * @return The "Get Code" button.
     */
    public Button getGetCodeButton() {

        return mGetCodeButton;
    }

    /**
     * Get the "Submit" button.
     *
     * @return The "Submit" button.
     */
    public Button getSubmitButton() {

        return mSubmitButton;
    }

    /**
     * Get the user registration code from the text view.
     *
     * @return The registration code as a string.
     */
    public String getUserRegistrationCode() {

        return mRegistrationCode.getText().toString();
    }

    /**
     * Sets the user registration code for the registration code text view.
     *
     * @param code The code to show in the text view.
     */
    public void setUserRegistrationCode(String code) {

        mRegistrationCode.setText(code);
    }

    /**
     * Method to get the registration code and display to user on the screen.
     */
    public abstract void getRegistrationCode();

    /**
     * Method to get authentication token for the device.
     * The method retries on its own to take care of failed calls, caller does not need to retry.
     */
    public abstract void getAuthenticationToken();

}
