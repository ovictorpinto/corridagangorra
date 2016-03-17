package br.com.r29tecnologia.corridagangorra;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "CorridaGangorra";
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private TextView tx;
    private int totalWidth;
    private int maxDeslocation;
    private float initialPosition;
    private float fator;
    private float position;
    private float maxIncliniacao = 12;
    private int stepMove = 20;
    private int delayNewCar = 2000;
    private int delayMoveCar = 100;
    private int carSize = 32;

    private View car;
    private RelativeLayout street;
    private Random random;
    private List<View> otherCars = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tx = (TextView) findViewById(R.id.textviewx);
        car = findViewById(R.id.car);
        street = (RelativeLayout) findViewById(R.id.street);

        random = new Random(new Date().getTime());
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        totalWidth = getScreenResolution(this);

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, carSize, r.getDisplayMetrics());

        maxDeslocation = totalWidth - (int) (px / 2);

        fator = totalWidth / maxIncliniacao;
        initialPosition = maxDeslocation / 2;
        position = initialPosition;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                addCar();
                handler.postDelayed(this, delayNewCar);
            }
        }, delayNewCar);

        final Handler handlerMove = new Handler();
        handlerMove.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveCars();
                handler.postDelayed(this, delayMoveCar);
            }
        }, delayMoveCar);

    }

    private void moveCars() {
        for (int i = 0, otherCarsSize = otherCars.size(); i < otherCarsSize; i++) {
            View otherCar = otherCars.get(i);

            RelativeLayout.LayoutParams actual = (RelativeLayout.LayoutParams) otherCar.getLayoutParams();
            RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(actual.width, actual.height);
            llp.setMargins(actual.leftMargin, actual.topMargin + stepMove, 0, 0); // llp.setMargins(left, top, right, bottom);

            otherCar.setLayoutParams(llp);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reset, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reset) {
            position = initialPosition;
            setCarPosition();
            otherCars.clear();
            street.removeAllViews();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSensorChanged(SensorEvent event) {
        Float x = event.values[0];
        Float y = event.values[1];
        Float z = event.values[2]; //rotacao relógio

        position += (fator * y);
        position = Math.max(position, 0);
        position = Math.min(position, maxDeslocation);
        tx.setText("Pos: " + position);

        setCarPosition();
    }

    private void setCarPosition() {
        RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(carSize, carSize);
        llp.setMargins((int) position, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
        llp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        car.setLayoutParams(llp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private static int getScreenResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        return width;
    }

    private void addCar() {

        int sortPosition = random.nextInt(maxDeslocation);

        View newCar = new View(this);
        newCar.setBackgroundColor(Color.GREEN);
        RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(carSize, carSize);
        llp.setMargins(sortPosition, 0, 0, 0);
        llp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        //        newCar.setLayoutParams(llp);

        otherCars.add(newCar);
        street.addView(newCar, llp);
    }
}
