package com.example.forgets.diedao;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import org.litepal.tablemanager.Connector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;


/**
 * Created by Forgets on 2016/10/20.
 */

public class SensorActivity extends Activity {
    private TextView textViewX = null;
    private TextView textViewY = null;
    private TextView textViewZ = null;
    private TextView textViewX1 = null;
    private TextView textViewY1 = null;
    private TextView textViewZ1 = null;
    private TextView textView1 = null;
    private TextView textView2 = null;
    private TextView textView3 = null;
    private TextView textView4 = null;
    private float acce[] = new float[3];
    private float gyro[] = new float[3];
    private float gravity[] = new float[3];
    private float linear_acceleration[] = new float[3];
    private float timestamp;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];

    private SensorManager accesensorManager = null;
    private Sensor accesensor = null;
    private SensorManager gyrosensorManager = null;
    private Sensor gyrosensor = null;


    /**
     * 合加速度
     */
   private float resultantAcceleration;

   private int counter = 0;
    private boolean isClick = false;
    /**
     * 训练模型
     */
   private svm_model model;

    private  svm_node data_acceleration;
    svm_node data_x ;
    svm_node data_y ;
    svm_node data_z ;

    private Button open;

   private List<Float> list = new ArrayList<Float>();

    private Data data;
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewX = (TextView) findViewById(R.id.TextView01);
        textViewY = (TextView) findViewById(R.id.TextView02);
        textViewZ = (TextView) findViewById(R.id.TextView03);
        textViewX1 = (TextView) findViewById(R.id.TextView04);
        textViewY1 = (TextView) findViewById(R.id.TextView05);
        textViewZ1 = (TextView) findViewById(R.id.TextView06);
        textView1 = (TextView) findViewById(R.id.TextView07);
        textView2 = (TextView) findViewById(R.id.TextView08);
        textView3 = (TextView) findViewById(R.id.TextView09);
        textView4 = (TextView) findViewById(R.id.TextView10);
        accesensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accesensor = accesensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyrosensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyrosensor = gyrosensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


//        Toast.makeText(SensorActivity.this,+"")
        /////////////////////////////////////////////////////

        data = new Data();
        String systemPath = getFilesDir().getAbsolutePath() + "/";
        String appFolderPath = systemPath+"fall/";
        String dataTrainPath = appFolderPath+ "fall-train-mm";
        //初始化svm训练模型
        try {
            File file = new File(dataTrainPath);
            if(!file.exists()){
            String path = saveAssetsFile();
            String[] trainArgs = { path};//directory of training file
//            System.out.println(trainArgs);
            model = svm_train.main(trainArgs);
            Log.d("SensorActivity", "onCreate: "+model);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Connector.getDatabase();
        //小心内存泄漏问题
        data_acceleration = new svm_node();
        data_x = new svm_node();
        data_y = new svm_node();
        data_z = new svm_node();
        open = (Button) findViewById(R.id.open);

  /*      Button open = (Button) findViewById(R.id.open);
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClick = true;
            }
        });*/


    }




    private SensorEventListener listener = new SensorEventListener() {

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }

        public void onSensorChanged(SensorEvent e) {
            if (timestamp != 0) {
                //final float alpha = (float) 0.8;
                final float dT = (e.timestamp - timestamp) * NS2S;
                double alpha = 0.1 / (0.1 + dT);
                gravity[0] = (float) (alpha * gravity[0] + (1 - alpha) * e.values[0]);
                gravity[1] = (float) (alpha * gravity[1] + (1 - alpha) * e.values[1]);
                gravity[2] = (float) (alpha * gravity[2] + (1 - alpha) * e.values[2]);

                linear_acceleration[0] = e.values[0] - gravity[0];
                linear_acceleration[1] = e.values[1] - gravity[1];
                linear_acceleration[2] = e.values[2] - gravity[2];

                textViewX.setText("X方向上加速度：       " + linear_acceleration[0] + "m/s^2");
                textViewY.setText("Y方向上加速度：       " + linear_acceleration[1] + "m/s^2");
                textViewZ.setText("Z方向上加速度：       " + linear_acceleration[2] + "m/s^2");

                resultantAcceleration = (float) Math.sqrt(linear_acceleration[0] * linear_acceleration[0] +
                        linear_acceleration[1] * linear_acceleration[1] + linear_acceleration[2] * linear_acceleration[2]);
                textView1.setText("合加速度：       " + resultantAcceleration + "m/s^2");
                textView3.setText("alpha：       " + alpha);




                    data.setX("2:" + linear_acceleration[0]);
                    data.setY("3:" + linear_acceleration[1]);
                    data.setZ("4:" + linear_acceleration[2]);
                    data.setAcceleration("1:" + resultantAcceleration);


                    // svm算法
                    //定义测试数据点c

                    data_acceleration.index = 1;
                    data_acceleration.value = resultantAcceleration;

                    data_x.index = 2;
                    data_x.value = linear_acceleration[0];

                    data_y.index = 3;
                    data_y.value = linear_acceleration[1];

                    data_z.index = 4;
                    data_z.value = linear_acceleration[2];

                    /**
                     * 预测
                     */
                    svm_node[] forecast = {data_acceleration, data_x, data_y, data_z}; //这个人的身高是185cm，体重是85kg,判断这个人的是男生，还是女生？

                    //预测测试数据的lable

                    try {
                        double r = svm.svm_predict(model, forecast);
//                        Toast.makeText(SensorActivity.this,r+"",Toast.LENGTH_SHORT);
                        data.setType(r);
                        if (12 == list.size()) {

                            open.setText("跌倒了");
                            list.clear();
                        }
                        if (1 == r && (resultantAcceleration - 8.825985) >= 0) {
                            list.add(resultantAcceleration);
                        } else {
                            list.clear();
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                data.save();
            }
            timestamp = e.timestamp;
        }
    };



    private SensorEventListener listener1 = new SensorEventListener() {
        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
        public void onSensorChanged(SensorEvent e1) {
            if (timestamp != 0) {
                final float dT = (e1.timestamp - timestamp) * NS2S;
                double alpha = 0.1/(0.1+dT);
                gyro[0] = e1.values[0];
                gyro[1] = e1.values[1];
                gyro[2] = e1.values[2];
                textViewX1.setText("X方向上陀螺仪值：       " + gyro[0] + "rad/s");
                textViewY1.setText("Y方向上陀螺仪值：       " + gyro[1] + "rad/s");
                textViewZ1.setText("Z方向上陀螺仪值：       " + gyro[2] + "rad/s");

                float b = (float) Math.sqrt(gyro[0] * gyro[0] + gyro[1] * gyro[1] + gyro[2] * gyro[2]);
                textView4.setText("合角速度：    " + b + "rad/s^2");
                textView2.setText("alpha：       " + alpha);

            }
            timestamp = e1.timestamp;
        }
    };

    protected void onResume() {
        super.onResume();
        accesensorManager.registerListener(listener, accesensor,
                5000);
        gyrosensorManager.registerListener(listener1, gyrosensor,
                5000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        accesensorManager.unregisterListener(listener);
        gyrosensorManager.unregisterListener(listener1);
    }



    //保存文件，将assert文件放入到手机里面去
    public String saveAssetsFile(){
        FileOutputStream out = null;
        InputStream inputStream = null;
        String dataTrainPath = null;
        String appFolderPath = null;
        try {
            inputStream =  getResources().getAssets().open("fall-train-mm");
            String systemPath = getFilesDir().getAbsolutePath() + "/";
            appFolderPath = systemPath+"fall/";
            dataTrainPath = appFolderPath+ "fall-train-mm";
            Log.d("MainActivity", "onCreate: "+dataTrainPath);
            File file = new File(appFolderPath);
            if(!file.exists()){
                file.mkdir();
            }

            out = new FileOutputStream(dataTrainPath);
            byte[] bytes = new byte[1024*5];
            int r;
            while ((r = inputStream.read(bytes)) != -1){
                out.write(bytes,0,r);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return dataTrainPath;
    }


}

