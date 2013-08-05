package cl.returnvoid.android.conmono.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;

import cl.returnvoid.android.conmono.ProcessImageActivity;
import cl.returnvoid.android.conmono.R;

/**
 * Created by ggio on 22-07-13.
 */
public class PreviewCameraFragment extends Fragment {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("THIS", "onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_preview_camera, container, false);
        //PreviewCameraFragment.PreviewCamera preview = (PreviewCameraFragment.PreviewCamera) getView().findViewById(R.id.preview_camera_view);
        //preview.setLayoutParams(new RelativeLayout.LayoutParams(300, 300));
        return view;
    }

    /**
     * PreviewCamera Class
     */

    public static class PreviewCamera extends SurfaceView implements SurfaceHolder.Callback{
        public static final String PREVIEW_CAMERA = "PREVIEW_CAMERA";
        private Boolean cameraConfigured = false;
        private Camera camera;
        public Uri imageSaved;

        public PreviewCamera(Context context, AttributeSet attrs){
            super(context, attrs);
            imageSaved = Uri.EMPTY;
            getHolder().addCallback(this);
        }

        public void capturePreviewCamera(){
            if(camera != null && getHolder().getSurface() != null){
                camera.takePicture(shutterCallback, pictureCallback, jpegCallBack);
            }
        }

        public void reactivateCamera(){
            camera.startPreview();
        }

        public void releaseCamera(){
            camera.stopPreview();
            camera.release();
        }

        public void openCamera(){
            Camera.open();
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
            initPreview(width, height);
            startPreview();
        }

        public void surfaceCreated(SurfaceHolder holder){
            camera = Camera.open();
        }

        public void surfaceDestroyed(SurfaceHolder holder){
            camera.stopPreview();
            camera = null;
        }

        @Override
        protected void onDraw(Canvas canvas){
            Log.d(PREVIEW_CAMERA, "On Draw Called");
        }

        Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                MediaPlayer shootMP = MediaPlayer.create(getContext(), Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
                shootMP.start();
            }
        };

        Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {

            }
        };

        Camera.PictureCallback jpegCallBack = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyPic");
                if(!folder.exists()){
                    folder.mkdir();
                }

                String photoName = "mypic" + String.format("_%d", System.currentTimeMillis()) + ".jpg";
                File photo = new File(folder, photoName);

                imageSaved = Uri.fromFile(photo);

                try {
                    FileOutputStream out = new FileOutputStream(photo.getPath());
                    Bitmap bmf = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    int w = 612;
                    int h = 612;
                    int width = bmf.getWidth();
                    int height = bmf.getHeight();
                    float scaleWidth = ((float) w) / width;
                    float scaleHeight = ((float) h) / height;
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleWidth, scaleHeight);
                    matrix.postRotate(90, 0, 0);

                    bmf.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    Log.d(PREVIEW_CAMERA, "imageSaved: " + imageSaved.toString());

                    Intent processImageActivity = new Intent(getContext(), ProcessImageActivity.class);
                    processImageActivity.putExtra("imaged_saved_uri", imageSaved.getPath());
                    getContext().startActivity(processImageActivity);
                }
                catch (java.io.IOException e) {
                    Log.e(PREVIEW_CAMERA, "Exception in photoCallback", e);
                }
                MediaScannerConnection.scanFile(getContext(),
                        new String[]{photo.getPath()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {

                            }
                        }
                );
                reactivateCamera();
            }
        };

        private void initPreview(int width, int height) {
            if (camera != null && getHolder().getSurface() != null) {
                if (!cameraConfigured) {
                    Camera.Parameters parameters = camera.getParameters();
                    Camera.Size size = getBestPreviewSize(width, height, parameters);

                    if (size != null) {
                        parameters.setPreviewSize(size.width, size.height);
                        camera.setParameters(parameters);
                        cameraConfigured = true;
                    }
                }
                try {
                    camera.setDisplayOrientation(90);
                    camera.setPreviewDisplay(getHolder());
                }
                catch (Throwable t) {
                    Log.d(PREVIEW_CAMERA, t.toString());
                }
            }
        }

        private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
            Camera.Size result = null;
            for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                if (size.width <= width && size.height <= height) {
                    if (result == null) {
                        result = size;
                    }
                    else {

                        int resultArea = result.width * result.height;
                        int newArea = size.width * size.height;
                        Log.d(PREVIEW_CAMERA, "NOT NULL: " + resultArea + " - " + newArea);
                        if (newArea > resultArea) {
                            result = size;
                        }
                    }
                }
            }

            return(result);
        }

        private void startPreview() {
            if (cameraConfigured && camera != null) {
                camera.startPreview();
            }
        }
    }
}