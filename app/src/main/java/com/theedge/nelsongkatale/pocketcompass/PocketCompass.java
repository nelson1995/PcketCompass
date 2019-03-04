package com.theedge.nelsongkatale.pocketcompass;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v7.widget.Toolbar;
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

//    private boolean buttontoggle=false;
//    private ImageButton flashLightButton;
////    boolean isflash=false;

    private Camera camera;
    private Camera.Parameters parameters;
    private ImageButton flashLightButton;
    boolean isFlashLightOn = false;
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

        if (isFlashSupport()) {
            camera = Camera.open();
            parameters = camera.getParameters();
        } else {
            showNoFlashAlert();
        }

        flashLightButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                int permission=PermissionChecker.checkSelfPermission(PocketCompass.this, Manifest.permission.CAMERA);
                if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && permission!= PermissionChecker.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
                }else{
                    if (!isFlashLightOn) {
                        toggleOn();
                    } else {
                        toggleOff();
                    }
                }
            }
        });

        Toolbar toolbar=(Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

    }


    /**toolbar*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.about,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.about_app:
                Intent intent=new Intent(this,AboutActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
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
    private void toggleOn(){
        try{
            flashLightButton.setImageResource(R.drawable.lighton);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();
            isFlashLightOn=true;

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /** method  sets flashlight off*/
    private  void toggleOff(){
        try{
            flashLightButton.setImageResource(R.drawable.lightoff);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            camera.stopPreview();
            isFlashLightOn=false;
        }catch (Exception ex){
            ex.printStackTrace();
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
    private void showNoFlashAlert() {
        new AlertDialog.Builder(this)
                .setMessage("Device hardware does not support flashlight!")
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).show();
    }

    private boolean isFlashSupport() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
}
