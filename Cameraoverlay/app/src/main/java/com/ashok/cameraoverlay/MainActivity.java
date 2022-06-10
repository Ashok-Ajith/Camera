package com.ashok.cameraoverlay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.hardware.Camera;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.widget.ImageButton;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.view.View.OnClickListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;



public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private Camera camera = null;
    private SurfaceView cameraSurfaceView = null;
    private SurfaceHolder cameraSurfaceHolder = null;
    private boolean previewing = false;
    ImageButton buttonCapture = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        cameraSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        cameraSurfaceHolder = cameraSurfaceView.getHolder();
        cameraSurfaceHolder.addCallback(this);
        cameraSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        buttonCapture = (ImageButton) findViewById(R.id.button1);
        ShutterCallback cameraShutterCallback = new ShutterCallback()
        {
            @Override
            public void onShutter()
            {
                // TODO Auto-generated method stub
            }
        };
        PictureCallback cameraPictureCallbackRaw = new PictureCallback()
        {
            @Override
            public void onPictureTaken(byte[] data, Camera camera)
            {
                // TODO Auto-generated method stub
            }
        };

        PictureCallback cameraPictureCallbackJpeg = new PictureCallback()
        {
            @Override
            public void onPictureTaken(byte[] data, Camera camera)
            {
                // TODO Auto-generated method stub
                Bitmap cameraBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                int   width = cameraBitmap.getWidth();
                int  height = cameraBitmap.getHeight();
                Toast.makeText(getApplicationContext(), width+""+height, Toast.LENGTH_SHORT).show();
                Bitmap newImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(newImage);
                canvas.drawBitmap(cameraBitmap, 0f, 0f, null);
                File storagePath = new File(Environment.getExternalStorageDirectory() + "/Sample Image/");
                storagePath.mkdirs();

                File myImage = new File(storagePath, Long.toString(System.currentTimeMillis()) + ".jpeg");

                try
                {
                    FileOutputStream out = new FileOutputStream(myImage);
                    newImage.compress(Bitmap.CompressFormat.JPEG, 80, out);
                    out.flush();
                    out.close();
                }
                catch(FileNotFoundException e)
                {
                    Log.d("In Saving File", e + "");
                }
                catch(IOException e)
                {
                    Log.d("In Saving File", e + "");
                }

                camera.startPreview();
                newImage.recycle();
                newImage = null;
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                                FileProvider.getUriForFile(getApplicationContext(),getPackageName() + ".provider", myImage) : Uri.fromFile(myImage),
                        "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
        };
        buttonCapture.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                camera.takePicture(cameraShutterCallback,cameraPictureCallbackRaw,cameraPictureCallbackJpeg);
            }
        });
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        try
        {
            camera = Camera.open();
        }
        catch(RuntimeException e)
        {
            Toast.makeText(getApplicationContext(), "Please enable permission to access camera", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if(previewing)
        {
            camera.stopPreview();
            previewing = false;
        }
        try
        {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(640, 480);
            parameters.setPictureSize(640, 480);
            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
            {
                camera.setDisplayOrientation(90);
            }

            parameters.setRotation(90);
            camera.setParameters(parameters);

            camera.setPreviewDisplay(cameraSurfaceHolder);
            camera.startPreview();
            previewing = true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        {
            camera.stopPreview();
            camera.release();
            camera = null;
            previewing = false;
        }
    }
}