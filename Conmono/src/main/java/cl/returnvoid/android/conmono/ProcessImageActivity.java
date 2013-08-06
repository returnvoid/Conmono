package cl.returnvoid.android.conmono;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;

public class ProcessImageActivity extends Activity {
    public static final String PROCESS_IMAGE_ACTIVITY = "PROCESS_IMAGE_ACTIVITY";
    protected ImageView imageView;
    protected File file;
    protected String uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_process_image);

        uri = getIntent().getStringExtra("imaged_saved_uri");
        Log.d(PROCESS_IMAGE_ACTIVITY, "uri: " + uri);

        imageView = (ImageView) findViewById(R.id.image);
        file = new File(uri);
        imageView.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
        Button applyEffect = (Button) findViewById(R.id.apply_effect);
        applyEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setImageBitmap(new ProcessImage().applyEffect());
            }
        });
        //imageView.setImageBitmap(new ProcessImage().applyEffect());
        //openDialog();
    }

    protected void openDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(getBaseContext());
        alert.setTitle("Texto");
        alert.setMessage("Ingresa tu mensaje");

        // Set an EditText view to get user input
        final EditText input;
        input = new EditText(getBaseContext());
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                Bitmap bm = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
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
        }catch (java.io.IOException e) {
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

    public class PreviewProcessedImage extends ImageView{

        private final int viewHeight;
        private final int viewWidth;

        public PreviewProcessedImage(Context context){
            super(context);
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            int screenWidth = display.getWidth();
            int screenHeight = display.getHeight();
            viewHeight = screenWidth;
            viewWidth = screenWidth;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

            setMeasuredDimension(viewWidth, viewWidth);
        }

    }

    /**
     * ProccessImage Class
     */
    public class ProcessImage{
        public void ProccessImage(){

        }

        public Bitmap applyEffect(){
            Bitmap bm = BitmapFactory.decodeFile(file.getPath());
            int []pixels = new int[bm.getWidth() * bm.getHeight()];
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
            Bitmap umbrella = BitmapFactory.decodeResource(getResources(), R.drawable.umbrella, options);
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
    }

    public class TextOnImage {
        public TextOnImage() {

        }

        public void textOnImage(Canvas canvas){
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
