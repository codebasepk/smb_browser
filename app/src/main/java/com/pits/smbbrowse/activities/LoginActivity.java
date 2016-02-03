package com.pits.smbbrowse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.pits.smbbrowse.R;
import com.pits.smbbrowse.utils.AppGlobals;
import com.pits.smbbrowse.utils.Constants;
import com.pits.smbbrowse.utils.Helpers;
import com.pits.smbbrowse.utils.UiHelpers;

public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener, TextWatcher {

    private EditText mHostEntry;
    private EditText mUsernameEntry;
    private EditText mPasswordEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mHostEntry = (EditText) findViewById(R.id.host_address_entry);
        mUsernameEntry = (EditText) findViewById(R.id.username_entry);
        mPasswordEntry = (EditText) findViewById(R.id.password_entry);

        mHostEntry.addTextChangedListener(this);
        mHostEntry.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mHostEntry.setSelection(Constants.HOST_PREFIX.length());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_submit:
                String hostnameFieldText = mHostEntry.getText().toString();
                String usernameFieldText = mUsernameEntry.getText().toString();
                String passwordFieldText = mPasswordEntry.getText().toString();

                // Ensure input fields are not empty
                if (hostnameFieldText.isEmpty() || usernameFieldText.isEmpty() ||
                        passwordFieldText.isEmpty()) {

                    String toastText = "All fields are required to be filled";
                    UiHelpers.showLongToast(getApplicationContext(), toastText);
                    return;
                }

                // Validate IP
                if (!Helpers.isValidIp(Helpers.getIpFromSambaUrl(hostnameFieldText))) {
                    String toastText = "Invalid IP address";
                    UiHelpers.showLongToast(getApplicationContext(), toastText);
                    return;
                }

                // If the address contains a directory, ensure it ends with a /
                if (!hostnameFieldText.endsWith("/")) {
                    hostnameFieldText = hostnameFieldText + "/";
                }

                if (Helpers.isWifiConnected(getApplicationContext())) {
                    UiHelpers.showWifiNotConnectedDialog(LoginActivity.this, true);
                }

                AppGlobals.setSambaHostAddress(hostnameFieldText);
                AppGlobals.setUsername(usernameFieldText);
                AppGlobals.setPassword(passwordFieldText);
                AppGlobals.setIsRunningForTheFirstTime(false);
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!s.toString().startsWith(Constants.HOST_PREFIX)) {
            mHostEntry.setText(Constants.HOST_PREFIX);
            mHostEntry.setSelection(Constants.HOST_PREFIX.length());
        }
    }
}
