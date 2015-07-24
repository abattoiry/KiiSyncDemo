package com.kii.sync;

import com.kii.cloud.storage.KiiUser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends Activity {

    private EditText usernameEdit;

    private EditText passwordEdit;

    private String mErrorString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEdit = (EditText)findViewById(R.id.username);
        passwordEdit = (EditText)findViewById(R.id.password);
        View v = findViewById(R.id.login);
        v.setOnClickListener(mClickListener);
        v = findViewById(R.id.register);
        v.setOnClickListener(mClickListener);

    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Activity activity = LoginActivity.this;
            if (activity == null) { return; }

            String[] text = new String[3];
            text[0] = usernameEdit.getText().toString();
            text[1] = passwordEdit.getText().toString();
            if (!KiiUser.isValidUserName(text[0])) {
                Toast.makeText(activity, "Username is not valid", Toast.LENGTH_LONG).show();
                return;
            }
            if (!KiiUser.isValidPassword(text[1])) {
                Toast.makeText(activity, "Password is not valid", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            switch (v.getId()) {
                case R.id.login:
                    text[2] = "login";
                    break;
                case R.id.register:
                    text[2] = "reg";
                    break;
            }
            new LoginOrRegTask().execute(text[0], text[1], text[2]);
        }
    };

    class LoginOrRegTask extends AsyncTask<String, Void, Void> {

        ProgressDialog dialog = null;
        String token = null;
        KiiUser user = null;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setMessage("In progress");
            dialog.setCancelable(false);
            dialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String type = params[2];
            try {
                if (type.equals("reg")) {
                    KiiUser.Builder builder = KiiUser.builderWithName(username);
                    user = builder.build();
                    user.register(password);
                } else {
                    user = KiiUser.logIn(username, password);
                }
                token = user.getAccessToken();
            } catch (Exception e) {
                token = null;
                mErrorString = e.getLocalizedMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            Activity activity = LoginActivity.this;
            if (activity == null) {
                return;
            }

            if (TextUtils.isEmpty(token)) {
                Toast.makeText(activity,
                        mErrorString,
                        Toast.LENGTH_LONG).show();
            } else {
                Utils.saveToken(activity.getApplicationContext(), token);
                Toast.makeText(activity, "Login successful",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
