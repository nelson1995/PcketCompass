package com.theedge.nelsongkatale.pocketcompass;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PocketCompass extends AppCompatActivity implements SensorEventListener {

    private boolean buttontoggle=false;
    private ImageButton flashLightButton;
    boolean isflash=false;
    private Camera camera;

    final int CAMERA_PERMISSION_CODE=1;

    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    private SensorManager mSensorManager;
    private TextView compassHeading;
    private ImageView dial;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flashLightButton = (ImageButton) findViewById(R.id.button);
        compassHeading=(TextView)findViewById(R.id.directionHeading);
        mSensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        dial=(ImageView)findViewById(R.id.compassDial);
        flashLightButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                int permission= PermissionChecker.checkSelfPermission(PocketCompass.this, Manifest.permission.CAMERA);
                if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= 23 && permission!= PermissionChecker.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);

                }else{
                    int buildSdkVersion=Build.VERSION.SDK_INT;
                    if (!isflash) {
                        if (!buttontoggle) {
                            if (buildSdkVersion >= Build.VERSION_CODES.LOLLIPOP) {
                                toggleOn();
                            }
                        } else {
                            toggleOff();
                        }
                    } else {
                        flashHardware();
                    }
                }
            }
        });

    }

    protected  void onResume(){

        super.onResume();
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(PocketCompass.this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //get angle around z-axis
        float degree=Math.round(event.values[0]);
        int degrees=(int)degree;
        compassHeading.setText(Integer.toString(degrees)+"°");
        RotateAnimation rotateAnimation=new RotateAnimation(currentDegree,-degree, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);

        //how long the animation will take place
        rotateAnimation.setDuration(210);

        //set the animation after the end of reservation status
        rotateAnimation.setFillAfter(true);

        //start animation
        dial.startAnimation(rotateAnimation);
        currentDegree=-degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /**method sets flashlight on*/
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void toggleOn(){
        CameraManager cameraManager=(CameraManager)getSystemService(Context.CAMERA_SERVICE);

        try {
            String flashID=cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(flashID,true);
            buttontoggle=true;
            flashLightButton.setImageResource(R.drawable.lighton);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /* method  sets flashlight off*/
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void toggleOff(){
        CameraManager cameraManager=(CameraManager)getSystemService(Context.CAMERA_SERVICE);

        try {
            String flashID=cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(flashID,false);
            buttontoggle=false;
            flashLightButton.setImageResource(R.drawable.lightoff);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**release camera resources on application exit*/
    protected  void onStop(){
        super.onStop();
        if(camera!=null){
            camera.release();
            camera=null;
        }
    }

    /**
     * display's dialog box when device doesn't support flash hardware
     * @return true when device doesn't have flash
     */
    private boolean flashHardware(){
        AlertDialog.Builder builder=new AlertDialog.Builder(PocketCompass.this);
        builder.setTitle("Error!");
        builder.setMessage("Device doesn't support flash");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.show();

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==CAMERA_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Toast.makeText(PocketCompass.this,"Permission Granted",Toast.LENGTH_SHORT).show();

            }else{

                Toast.makeText(PocketCompass.this,"Permission Denied ! ",Toast.LENGTH_SHORT).show();
            }

        }
    }
}
