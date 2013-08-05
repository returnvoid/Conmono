package cl.returnvoid.android.conmono;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.facebook.Request;
import cl.returnvoid.android.conmono.fragments.PreviewCameraFragment;

public class PreviewCameraActivity extends FragmentActivity {
    private String PREVIEW_CAMERA_ACTIVITY_TAG = "PREVIEW_CAMERA_ACTIVITY_TAG";
    public PreviewCameraFragment previewCameraFragment;
    PreviewCameraFragment.PreviewCamera previewCamera;
    private Request request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_preview_camera);

        Display display = getWindowManager().getDefaultDisplay();

        previewCameraFragment = (PreviewCameraFragment) getSupportFragmentManager().findFragmentById(R.id.preview_camera_fragment);
        previewCamera = (PreviewCameraFragment.PreviewCamera) findViewById(R.id.preview_camera_view);
        //previewCamera.setLayoutParams(new RelativeLayout.LayoutParams(100, 250));

        Button shutterButton = (Button) findViewById(R.id.shutter_button);
        shutterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(PREVIEW_CAMERA_ACTIVITY_TAG, "BARKING!");
                capturePreview();
            }
        });
    }

    protected void capturePreview(){
        previewCamera.capturePreviewCamera();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            Log.d(PREVIEW_CAMERA_ACTIVITY_TAG, "onKeyDown: ");
            //finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(PREVIEW_CAMERA_ACTIVITY_TAG, "onDestroy");
       // previewCameraFragment.getPreview().releaseCamera();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.preview_camera, menu);
        return true;
    }
    
}
