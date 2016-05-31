package com.kseek.opencvsamples.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.kseek.opencvsamples.utils.Puzzle15Processor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class Puzzle15Activity extends AppCompatActivity implements CvCameraViewListener, View
        .OnTouchListener
{

    private static final String TAG = "Puzzle15";

    private CameraBridgeViewBase openCvCameraView;
    private Puzzle15Processor puzzle15;
    private MenuItem itemHideNumbers;
    private MenuItem itemStartNewGame;


    private int gameWidth;
    private int gameHeight;

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this)
    {

        @Override
        public void onManagerConnected(int status)
        {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    /* Now enable camera view to start receiving frames */
                    openCvCameraView.setOnTouchListener(Puzzle15Activity.this);
                    openCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d(TAG, "Creating and setting view");
        openCvCameraView = new JavaCameraView(this, -1);
        setContentView(openCvCameraView);
        openCvCameraView.setCvCameraViewListener(this);
        puzzle15 = new Puzzle15Processor();
        puzzle15.prepareNewGame();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (openCvCameraView != null)
            openCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for " +
                    "initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy()
    {
        super.onDestroy();
        if (openCvCameraView != null)
            openCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.i(TAG, "called onCreateOptionsMenu");
        itemHideNumbers = menu.add("Show/hide tile numbers");
        itemStartNewGame = menu.add("Start new game");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.i(TAG, "Menu Item selected " + item);
        if (item == itemStartNewGame) {
            /* We need to start new game */
            puzzle15.prepareNewGame();
        } else if (item == itemHideNumbers) {
            /* We need to enable or disable drawing of the tile numbers */
            puzzle15.toggleTileNumbers();
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return true;
    }

    public void onCameraViewStarted(int width, int height)
    {
        gameWidth = width;
        gameHeight = height;
        puzzle15.prepareGameSize(width, height);
    }

    public void onCameraViewStopped()
    {
    }

    public boolean onTouch(View view, MotionEvent event)
    {
        int xpos, ypos;

        xpos = (view.getWidth() - gameWidth) / 2;
        xpos = (int) event.getX() - xpos;

        ypos = (view.getHeight() - gameHeight) / 2;
        ypos = (int) event.getY() - ypos;

        if (xpos >= 0 && xpos <= gameWidth && ypos >= 0 && ypos <= gameHeight) {
            /* click is inside the picture. Deliver this event to processor */
            puzzle15.deliverTouchEvent(xpos, ypos);
        }

        return false;
    }

    public Mat onCameraFrame(Mat inputFrame)
    {
        return puzzle15.puzzleFrame(inputFrame);
    }
}
