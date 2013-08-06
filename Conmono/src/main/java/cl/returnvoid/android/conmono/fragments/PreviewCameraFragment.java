package cl.returnvoid.android.conmono.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;

import cl.returnvoid.android.conmono.ProcessImageActivity;
import cl.returnvoid.android.conmono.R;

/**
 * Created by ggio on 22-07-13.
 */
public class PreviewCameraFragment extends Fragment {
    public static ProgressDialog progressDialog;
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
        return view;
    }

    /**
     * PreviewCamera Class
     */

    public static class PreviewCamera extends SurfaceView implements SurfaceHolder.Callback{
        public static final String PREVIEW_CAMERA = "PREVIEW_CAMERA";
        private final int viewHeight;
        private final int viewWidth;
        private Boolean cameraConfigured = false;
        private Camera camera;
        public Uri imageSaved;

        public PreviewCamera(Context context, AttributeSet attrs){
            super(context, attrs);
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            int screenWidth = display.getWidth();
            int screenHeight = display.getHeight();
            viewHeight = 2 *  (screenHeight / 3);
            viewWidth = screenWidth;

            imageSaved = Uri.EMPTY;
            getHolder().addCallback(this);
        }

        @Override
        public void onLayout(boolean changed, int left, int top, int right, int bottom) {
            if (changed) {
                (this).layout(0, 0, viewWidth, viewHeight);
            }
        }

        public void capturePreviewCamera(){
            if(camera != null && getHolder().getSurface() != null){
                camera.takePicture(shutterCallback, null, jpegCallBack);
            }
        }

        public void reactivateCamera(){
            camera.startPreview();
        }

        public void releaseCamera(){
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
            releaseCamera();
        }

        Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                MediaPlayer shootMP = MediaPlayer.create(getContext(), Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
                shootMP.start();
            }
        };

        Camera.PictureCallback jpegCallBack = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                savePictureOnSDCard(bytes);
            }
        };

        public interface CallBackPictureSaved{
            public void onPictureSaved(Uri imageSaved);
        }

        private void savePictureOnSDCard(byte[] bytes){
            SavePictureOnSDCard savePictureOnSDCard = new SavePictureOnSDCard(getContext(), new CallBackPictureSaved(){
                @Override
                public void onPictureSaved(Uri imageSaved){
                    Log.d(PREVIEW_CAMERA, "savePictureOnSDCard callback:"+imageSaved.toString());
                    MediaScannerConnection.scanFile(getContext(),
                            new String[]{imageSaved.getPath()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.d(PREVIEW_CAMERA, "SCAN COMPLETED");
                                    //reactivateCamera();
                                }
                            }
                    );
                    Intent processImageActivity = new Intent(getContext(), ProcessImageActivity.class);
                    processImageActivity.putExtra("imaged_saved_uri", imageSaved.getPath());
                    getContext().startActivity(processImageActivity);
                }
            });
            savePictureOnSDCard.execute(bytes);
        }

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

    static class SavePictureOnSDCard extends AsyncTask<byte[], Integer, Uri>{
        private static final String PREVIEW_CAMERA = "SAVE_PICTURE_ON_SD_CARD";
        private PreviewCamera.CallBackPictureSaved callBackPictureSaved;
        private ProgressDialog progressDialog;
        private Context context;


        public SavePictureOnSDCard(Context context, PreviewCamera.CallBackPictureSaved callBackPictureSaved) {
            this.callBackPictureSaved = callBackPictureSaved;
            this.context = context;
            progressDialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute(){
            progressDialog.setMessage(context.getResources().getString(R.string.dialog_picture_saved));
            progressDialog.show();
        }
        @Override
        protected Uri doInBackground(byte[]... bytes){
            byte[] imageBytes = bytes[0];
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ConMono");
            if(!folder.exists()){
                folder.mkdir();
            }

            String photoName = "conmono" + String.format("_%d", System.currentTimeMillis()) + ".jpg";
            File photo = new File(folder, photoName);

            Uri imageSaved = Uri.fromFile(photo);
            try {
                FileOutputStream out = new FileOutputStream(photo.getPath());
                Bitmap bmf = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
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
            }
            catch (java.io.IOException e) {
                Log.e(PREVIEW_CAMERA, "Exception in photoCallback", e);
            }

            if(isCancelled()) return null;
            return imageSaved;
        }

        @Override
        protected void onProgressUpdate(Integer... progress){

        }

        @Override
        protected void onPostExecute(Uri result) {
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            callBackPictureSaved.onPictureSaved(result);
        }
    }
}