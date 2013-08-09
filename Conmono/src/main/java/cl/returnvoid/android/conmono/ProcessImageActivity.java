package cl.returnvoid.android.conmono;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class ProcessImageActivity extends Activity {
    public static final String PROCESS_IMAGE_ACTIVITY = "PROCESS_IMAGE_ACTIVITY";
    protected ImageView imageForProcess;
    protected String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_process_image);

        uri = getIntent().getStringExtra("imaged_saved_uri");

        imageForProcess = (ImageView) findViewById(R.id.conmono_image_for_process);
        LoadPictureFromSDCard loadPictureFromSDCard = new LoadPictureFromSDCard(new CallBackPictureLoaded() {
            @Override
            public void onPictureLoaded(Bitmap bm) {
                loadOriginalPictureFromFile(bm);
            }
        });
        loadPictureFromSDCard.execute(uri);

    }

    public interface CallBackPictureLoaded {
        public void onPictureLoaded(Bitmap bm);
    }

    private void loadOriginalPictureFromFile(final Bitmap bm) {
        imageForProcess.setImageBitmap(Bitmap.createScaledBitmap(bm, 612, 612, true));
        ImageButton glitchButton = (ImageButton) findViewById(R.id.glitch_button);
        glitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyGlitch(bm);
            }
        });
    }

    private void applyGlitch(Bitmap bm){
        imageForProcess.setImageBitmap(new ProcessImage().glitch(bm));
    }

    protected void openDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getBaseContext());
        alert.setTitle("Texto");
        alert.setMessage(getResources().getString(R.string.msg));

        // Set an EditText view to get user input
        final EditText input;
        input = new EditText(getBaseContext());
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                Bitmap bm = ((BitmapDrawable) imageForProcess.getDrawable()).getBitmap();
                saveNewFile(bm);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    protected void saveNewFile(Bitmap bitmap) {
        try {
            FileOutputStream out = new FileOutputStream(uri);
            Bitmap bmf = bitmap;
            int w = 612;
            int h = 612;

            Bitmap mutableBm = bmf.createBitmap(bmf, 0, 0, w, h);

            Canvas canvas = new Canvas(mutableBm);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            canvas.drawBitmap(mutableBm, 0, 0, paint);

            mutableBm.compress(Bitmap.CompressFormat.JPEG, 90, out);
        } catch (java.io.IOException e) {
            Log.e(PROCESS_IMAGE_ACTIVITY, "Exception in photoCallback", e);
        }
        MediaScannerConnection.scanFile(getBaseContext(),
                new String[]{uri}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {

                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.process_image, menu);
        return true;
    }

    public static class PreviewProcessedImage extends ImageView {


        public PreviewProcessedImage(Context context) {
            super(context);

        }

        public PreviewProcessedImage(Context context, AttributeSet attribs) {
            super(context, attribs);

        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        }

    }

    static class LoadPictureFromSDCard extends AsyncTask<String, Void, Bitmap> {
        private CallBackPictureLoaded callBackPictureLoaded;

        public LoadPictureFromSDCard(CallBackPictureLoaded callBackPictureLoaded) {
            this.callBackPictureLoaded = callBackPictureLoaded;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            File file = new File(params[0]);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getPath(), options);
            options.inSampleSize = calculateInSampleSize(options, 612, 612);
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);
            Bitmap bitmapMutable = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            Canvas canvas = new Canvas(bitmapMutable);
            Matrix matrix = new Matrix();
            matrix.postRotate(90, 0, 0);
            canvas.setMatrix(matrix);
            Bitmap bm = Bitmap.createBitmap(bitmapMutable, 0, 0, 612, 612, matrix, true);

            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            callBackPictureLoaded.onPictureLoaded(bm);
        }

        public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
            return inSampleSize;
        }
    }

    /**
     * ProccessImage Class
     */
    public class ProcessImage {
        public void ProcessImage() {

        }

        public Bitmap applyEffect() {
            File file = new File(uri);
            Bitmap bm = BitmapFactory.decodeFile(file.getPath());
            int[] pixels = new int[bm.getWidth() * bm.getHeight()];
            int k = 0;
            /*for(int i = 0; i < bm.getWidth(); i++){
                for(int j = 0; j < bm.getHeight(); j++, k++){
                    int pixel = bm.getPixel(i, j);
                    int red = Color.red(pixel);
                    if( red > 100 ){
                        pixels[k] = Color.BLUE;
                    }else{
                        pixels[k] = pixel;
                    }
                }
            }*/
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap base = Bitmap.createBitmap(bm, 0, 0, 612, 612);//Bitmap.createBitmap(bm, 612, 612, bm.getConfig());
            Bitmap umbrella = BitmapFactory.decodeResource(getResources(), R.drawable.conmono_logotipo, options);
            Bitmap result = Bitmap.createBitmap(612, 612, Bitmap.Config.ARGB_8888);

            Paint paintBase = new Paint();
            //paintBase.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY));
            //paintBase.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));

            Paint paintUmbrella = new Paint();
            paintUmbrella.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD));
            paintUmbrella.setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.DARKEN));
            paintUmbrella.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(base, 0, 0, paintBase);
            canvas.drawBitmap(umbrella, 0, 0, paintUmbrella);
            //new TextOnImage().textOnImage(canvas);
            return result;
        }

        public Bitmap glitch(Bitmap bm) {
            int MARGIN = 30;
            int OFFSET = 3;
            Bitmap bitmapOriginal = bm.copy(Bitmap.Config.ARGB_8888, true);
            int[] pixels = new int[bitmapOriginal.getWidth() * bitmapOriginal.getHeight()];
            int k = 0;
            for (int i = 0; i < bitmapOriginal.getWidth() - 60; i++) {
                for (int j = 0; j < bitmapOriginal.getHeight() - 60; j++, k++) {
                    int pixel = bm.getPixel(i, j);
                    int red = Color.red(pixel);
                    if (red > 100) {
                        pixels[k] = Color.YELLOW;
                    } else {
                        pixels[k] = pixel;
                    }
                }
            }

            Paint paint = new Paint();
            paint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.OVERLAY));
            paint.setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SCREEN));
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));

            Canvas canvas = new Canvas(bitmapOriginal);
            Rect rect = new Rect(MARGIN + OFFSET, MARGIN + OFFSET, bitmapOriginal.getWidth() - MARGIN, bitmapOriginal.getHeight() - MARGIN);
            canvas.drawBitmap(bitmapOriginal, rect, rect, paint);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap frame = BitmapFactory.decodeResource(getResources(), R.drawable.frame1, options);

            paint.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.CLEAR));
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
            //canvas.drawBitmap(frame, 0, 0, null);

            //Bitmap bitmap = Bitmap.createBitmap(pixels, 0, bitmapOriginal.getWidth(), bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), Bitmap.Config.RGB_565);

            return bitmapOriginal;
        }
    }


    public class TextOnImage {
        public TextOnImage() {

        }

        public void textOnImage(Canvas canvas) {
            Bitmap bm = Bitmap.createBitmap(612, 612, Bitmap.Config.ARGB_8888);
            Typeface type = Typeface.create("Roboto", Typeface.BOLD);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE);
            paint.setTypeface(type);
            paint.setTextSize(60);
            canvas.drawText("what", 60, 612 - 60, paint);

        }
    }

}
