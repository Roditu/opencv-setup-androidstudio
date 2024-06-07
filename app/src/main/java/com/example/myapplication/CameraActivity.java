package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.Manifest;
//import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";

    private Mat mRgba;
    private Mat mGrey;

    private CameraBridgeViewBase mOpenCvCameraView;
    private ImageView flip_camera;
    private int mCameraId=0; //back camera
//    private int mCameraId=1; //front camera

    private CascadeClassifier cascadeClassifier;
    private CascadeClassifier cascadeClassifier_eye;

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCv Is Loaded");
                    mOpenCvCameraView.enableView();
                }
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private void swapCamera() {
        mCameraId=mCameraId^1;

        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(mCameraId);
        mOpenCvCameraView.enableView();

    }

    public CameraActivity(){
        Log.i(TAG, "Instantiated new "+this.getClass());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

        int MY_PERMISSIONS_REQUEST_CAMERA = 0;
//        int MY_CAMERA_REQUEST_CODE = 100;
        int activeCamera = CameraBridgeViewBase.CvCameraViewListener2;
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.frame_surface);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableFpsMeter();

        flip_camera=findViewById(R.id.flip_camera);
        flip_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapCamera();
            }
        });

        if(ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED){
            Log.d(TAG, "Permissions granted");
            mOpenCvCameraView.setCameraPermissionGranted();
            mOpenCvCameraView.setCameraIndex(activeCamera);
            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }else {
            // prompt system dialog
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

//        Load model
        try{
//            Face Detection
            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir=getDir("cascade", Context.MODE_PRIVATE); // creating folder
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");    // creating file
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer=new byte[4096];
            int byteRead;
            while((byteRead = is.read(buffer)) != -1){
                os.write(buffer, 0, byteRead);
            }
            is.close();
            os.close();

//            Load file from cascade folder that have been loaded
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());

//            Eye Detection
            InputStream is2 = getResources().openRawResource(R.raw.haarcascade_eye);

            File mCascadeFile_eye = new File(cascadeDir, "haarcascade_eye.xml");    // creating file
            FileOutputStream os2 = new FileOutputStream(mCascadeFile_eye);
            byte[] buffer2=new byte[4096];
            int byteRead2;
            while((byteRead2 = is2.read(buffer2)) != -1){
                os2.write(buffer2, 0, byteRead2);
            }
            is2.close();
            os2.close();

//            Load file from cascade folder that have been loaded
            cascadeClassifier_eye = new CascadeClassifier(mCascadeFile_eye.getAbsolutePath());

        }catch(IOException e){
            Log.i(TAG, "Cascade file not found");
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int activeCamera = CameraBridgeViewBase.CvCameraViewListener2;
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // camera can be turned on
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                mOpenCvCameraView.setCameraPermissionGranted();
                mOpenCvCameraView.setCameraIndex(activeCamera);
                mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
                mOpenCvCameraView.setCvCameraViewListener(this);
            } else {
                // camera will stay off
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.d(TAG, "Opencv Initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        }else{
            Log.d(TAG, "Opencv is not loaded. Try Again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();

        }
    }

    public void onCameraViewStarted(int width, int height){
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGrey=new Mat(height,width, CvType.CV_8UC1);

    }

    public void onCameraViewStopped(){
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba=inputFrame.rgba();
        mGrey=inputFrame.gray();

        if(mCameraId==1){
            Core.flip(mRgba,mRgba,-1);
            Core.flip(mGrey,mGrey,-1);

        }

//        processing pass mRgba to cascade class
        mRgba=CascadeRec(mRgba);

        return mRgba;

    }

    private Mat CascadeRec(Mat mRgba) {

        Core.flip(mRgba.t(),mRgba,1);
        Mat mRbg = new Mat();
        Imgproc.cvtColor(mRgba,mRbg,Imgproc.COLOR_RGBA2RGB);

        int height = mRbg.height();

        int absoluteFaceSize=(int) (height*0.1); //minimum size of face in frame

//        Minimum size of output
        MatOfRect faces = new MatOfRect();
        if(cascadeClassifier != null){
            cascadeClassifier.detectMultiScale(mRbg,faces,1.1,2,2, new Size(absoluteFaceSize,absoluteFaceSize), new Size());
        }

        Rect[] facesArray=faces.toArray();
        for (int i=0; i<facesArray.length; i++) {
//            Draw face on original frame mRgba
//            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 2);

//            Crop face image and pass it to eye class
            Rect roi = new Rect((int) facesArray[i].tl().x,(int)facesArray[i].tl().y,
                    (int) facesArray[i].br().x-(int) facesArray[i].tl().x,
                    (int) facesArray[i].br().y-(int) facesArray[i].tl().y);

            Mat cropped = new Mat(mRgba,roi);

            MatOfRect eyes = new MatOfRect(); //Pass to classifier

            if(cascadeClassifier_eye!=null){
                cascadeClassifier_eye.detectMultiScale(cropped,eyes,1.15,2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT | Objdetect.CASCADE_SCALE_IMAGE,
                        // Minimum size of eye
                        new Size(35,35), new Size());

                // Array of eyes
                Rect[] eyesarray = eyes.toArray();

                for(int j=0;j<eyesarray.length;j++){
//                    find on original frame
                    int x1 = (int) (eyesarray[j].tl().x+facesArray[i].tl().x);  //starting point
                    int y1 = (int) (eyesarray[j].tl().y+facesArray[i].tl().y);
                    int w1 = (int) (eyesarray[j].br().x-eyesarray[j].tl().x);  //width and height
                    int h1 = (int) (eyesarray[j].br().y-eyesarray[j].tl().y);
                    int x2=(int) (w1+x1);                                       //end point
                    int y2=(int) (h1+y1);

//                    Draw eye on frame
                    Imgproc.rectangle(mRgba,new Point(x1,y1), new Point(x2,y2),new Scalar(0,255,0), 2);

////                    Pupil
////                    Crop eye
//                    Rect eye_roi = new Rect(x1+5,y1+22,w1-5,h1-10);
//                    Mat eye_cropped=new Mat(mRgba,eye_roi);
//
////                    Convert gray
//                    Mat Gray_eye=new Mat();
//                    Imgproc.cvtColor(eye_cropped,Gray_eye,Imgproc.COLOR_RGBA2GRAY);
//                    Imgproc.blur(Gray_eye,Gray_eye,new Size(5,5));  //blue
//                    Imgproc.threshold(Gray_eye,Gray_eye,110,255,Imgproc.THRESH_BINARY_INV); //threshold layer 2 Inverse binary image
//
//                    Imgproc.cvtColor(Gray_eye,Gray_eye,Imgproc.COLOR_GRAY2RGBA);
//                    Core.add(Gray_eye,eye_cropped,eye_cropped);
//
//                    eye_cropped.copyTo(new Mat(mRgba,eye_roi));

                }

            }
        }
        Core.flip(mRgba.t(),mRgba,0);

//        Eye Detection

        return mRgba;
    }
}

//    To convert to gray
//        Imgproc.cvtColor(mRgba,mRgba,Imgproc.COLOR_RGBA2GRAY);
//        or Just return the gray
//        return mGrey;

//        To get BGRa display
//         Imgproc.cvtColor(mRgba,mRgba,Imgproc.COLOR_RGBA2BGRA);


//    //        To detect edges with canny
//    Mat edges = new Mat();
//        Imgproc.Canny(mRgba,edges,100,200);
////        then detect lines
//                Mat lines = new Mat();
//                Point p1 = new Point();
//                Point p2 = new Point();
//                double a,b;
//                double x0,y0;
//                Imgproc.HoughLines(edges,lines,1.0,Math.PI/180.0, 140);
//
//                for (int i=0; i<lines.rows();i++){
////            for each line
//        double[]vec= lines.get(i, 0);
//        double rho=vec[0];
//        double theta=vec[1];
//        a=Math.cos(theta);
//        b=Math.sin(theta);
//        x0=a*rho;
//        y0=b*rho;
//
//        p1.x=Math.round(x0+1000*(-b));
//        p1.y=Math.round(y0+1000*a);
//        p2.x=Math.round(x0+1000*(-b));
//        p2.y=Math.round(y0+1000*a);
//
////            Draw Line
//        Imgproc.line(mRgba,p1,p2, new Scalar(255.0,255.0,255.0), 1, Imgproc.LINE_AA,0);
//        }
