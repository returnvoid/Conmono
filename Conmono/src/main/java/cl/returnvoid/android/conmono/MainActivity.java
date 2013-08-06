package cl.returnvoid.android.conmono;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.facebook.Session;
import com.facebook.SessionState;


public class MainActivity extends FragmentActivity{
    private String ACTIVITY_TAG = "ACTIVITY_TAG";
    private Session.StatusCallback statusCallback = new SessionStatusCallback();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //this is main activity
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        Session session = Session.getActiveSession();
        Log.d(ACTIVITY_TAG, "onCreate");
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
        }

        if(session.isOpened()){
            //goToPreviewCamera();
        }

        updateView();
    }

    private void updateView() {
        Button facebookLogButton = (Button) findViewById(R.id.facebook_login_button);
        Session session = Session.getActiveSession();
        Log.d(ACTIVITY_TAG, "updateView: " + session.isOpened());
        if (session.isOpened()) {
            //facebookLogButton.setText(R.string.facebook_login_button_label);
            facebookLogButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) { onClickLogout(); }
            });
        } else {
            //facebookLogButton.setText(R.string.facebook_logout_button_label);
            facebookLogButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) { onClickLogin(); }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    private void onClickLogin() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        } else {
            Session.openActiveSession(this, true, statusCallback);
        }
    }

    private void onClickLogout() {
        Session session = Session.getActiveSession();
        if (!session.isClosed()) {
            session.closeAndClearTokenInformation();
            updateView();
        }
    }

    private void goToPreviewCamera(){
        Intent previewCameraActivity = new Intent(MainActivity.this, PreviewCameraActivity.class);
        startActivity(previewCameraActivity);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }


    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            updateView();
            Log.d(ACTIVITY_TAG, "call: " + session.isOpened() + " " + state.isOpened());
            if(session.isOpened()){
                goToPreviewCamera();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(ACTIVITY_TAG, "onResume");
        updateView();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent prefs = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(prefs);
            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
