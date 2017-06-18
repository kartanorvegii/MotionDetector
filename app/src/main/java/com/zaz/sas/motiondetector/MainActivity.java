package com.zaz.sas.motiondetector;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private Object currentFrame;
    private Object previousFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initDebug();
        mOpenCvCameraView.enableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        currentFrame = inputFrame.rgba();
        try{
            currentFrame = detectMotion((Mat)currentFrame, (Mat)previousFrame);
        }catch (Exception e){
            currentFrame = new Mat();
            previousFrame = new Mat();
        }
        previousFrame = inputFrame.rgba();
        return (Mat) currentFrame;
    }

    public Mat detectMotion(Mat currentFrame, Mat previousFrame){
        int sensivity = 30;
        double maxArea = 30;
        Mat vector = new Mat();
        Scalar color1 = new Scalar(0, 0, 255);
        Scalar color2 = new Scalar(0, 255, 0);
        Size size = new Size(3, 3);
        Mat frame = new Mat();
        Mat resultFrame = new Mat();
        Imgproc.GaussianBlur(currentFrame, currentFrame, size, 0);
        Core.subtract(previousFrame, currentFrame, resultFrame);
        Imgproc.cvtColor(resultFrame, resultFrame, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(resultFrame, resultFrame, sensivity, 255, Imgproc.THRESH_BINARY);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(resultFrame, contours, vector, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        vector.release();

        for(int i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            double contourarea = Imgproc.contourArea(contour);
            if(contourarea > maxArea) {
                Rect r = Imgproc.boundingRect(contours.get(i));
                Imgproc.drawContours(frame, contours, i, color1);
                Imgproc.rectangle(frame, r.br(), r.tl(), color2, 1);
            }
            contour.release();
        }
        return frame;
    }
}
