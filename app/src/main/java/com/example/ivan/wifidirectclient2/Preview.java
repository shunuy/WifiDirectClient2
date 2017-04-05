package com.example.ivan.wifidirectclient2;

import android.content.Context;

import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

//Preview class used to start the camera preview
public class Preview extends GLSurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{

    private static final String TAG = "NEUTRAL";

    SurfaceHolder                   mHolder;
    Camera                          mCamera;
    Camera.Parameters               param;
    Camera.Size                     previewSize;
    int[]                           fpsSelected;

    List<Camera.Size>               resSize;
    List<int[]>                     fpsList;
    List<String>                    fpsString = new ArrayList<String>();
    List<String>                    resString = new ArrayList<String>();

    int                             count = 0;
    boolean                         skip_frame = true;

    //DATA TRANSMISSION
    DataManagement dm;

    public Preview(Context context) {
        super(context);

        safeCameraOpen();

    }

    private boolean safeCameraOpen() {
        boolean qOpened = false;

        try {
            mCamera = Camera.open();
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.d(TAG, "failed to open Camera: " + e.toString());
        }

        mHolder=getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        return qOpened;
    }

    private boolean safeClose(){
        boolean qClosed = false;
        try {
            mCamera.stopPreview();
            mCamera.release();
            //mCamera = null;
        } catch (Exception e) {
            Log.d(TAG, "failed to open Camera: " + e.toString());
        }

        return qClosed;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try{
            Log.d(TAG,"Surface Created");
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(this);
        }catch(Exception e) {
            Log.d(TAG, "Error setting holder: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        param = mCamera.getParameters();

        // THE CONFIGURATION BELOW IS SPECIFICALLY FOR THE BT-2000 WHERE NORMAL
        // CAMERA CONFIGURATION WILL NOT WORK
        /*
        List<String> epson_supported = param.getSupportedEpsonCameraModes();
        Log.d(TAG, "Supported Modes:");
        for (String s : epson_supported){
            Log.d(TAG, "Mode: " + s);
        }
        Log.d(TAG,"Selected Mode: " + epson_supported.get(2));
        param.setEpsonCameraMode(epson_supported.get(2));
        previewSize = getSmallestPreviewSize();
        */

        // UPDATE AVAILABLE CAMERA CONFIGURATION
        getFPS();
        getResSize();

        // DEFAULT PARAMETERS
        previewSize = resSize.get(0);
        fpsSelected = fpsList.get(0);


        // FOR SAMSUNG S4
        // previewSize = resSize.get(7);
        // fpsSelected = fpsList.get(0);
        // fpsSelected[0] = 15000;

        // FOR BT-200
        // previewSize = resSize.get(1);
        // fpsSelected = fpsList.get(3);

        //Constant for NV21 format is 17
        //param.setPreviewFormat(17);
        param.setPreviewFpsRange(fpsSelected[0],fpsSelected[1]);
        param.setPreviewSize(previewSize.width,previewSize.height);
        mCamera.setDisplayOrientation(0);
        //For Portrait modes: mCamera.setDisplayOrientation(90);
        mCamera.setParameters(param);
        dm.setImageSize(previewSize.width, previewSize.height);

        try{
            mCamera.startPreview();
            Log.d(TAG, "Camera Preview Size: " + previewSize.width + " x " + previewSize.height);
            Log.d(TAG, "Preview Format: " + param.getPreviewFormat());
        }catch(Exception e){
            Log.d(TAG,"Error starting preview");
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            Log.d(TAG,"Surface Destroyed");
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }catch(Exception e){
            Log.d(TAG,"Error stoppipng camera on surface destroy");
        }
    }

    private Camera.Size getBestPreviewSize (){

        Camera.Size result = null;
        Camera.Parameters param = mCamera.getParameters();
        for (Camera.Size size : param.getSupportedPreviewSizes()){
            if (result==null){result=size;}
            else{
                int resultArea = result.width*result.height;
                int newArea = size.width*size.height;

                if (newArea>resultArea){
                    result = size;
                }
            }
        }

        return result;
    }

    private Camera.Size getSmallestPreviewSize (){

        Camera.Size result = null;
        Camera.Parameters param = mCamera.getParameters();
        for (Camera.Size size : param.getSupportedPreviewSizes()){
            if (result==null){result=size;}
            else{

                int resultArea = result.width*result.height;
                int newArea = size.width*size.height;

                if (newArea<resultArea){
                    result = size;
                }
            }
        }

        return result;
    }

    public void getFPS(){
        try{
            fpsList=param.getSupportedPreviewFpsRange();
            int x1 = 0;
            for (int[] x: fpsList) {
                int[] y = fpsList.get(x1);
                x1 = x1 + 1;
                fpsString.add(y[0] + " to " + y[1]);
                Log.d(TAG, "FPS List: " + y[0] + "," + y[1]);
            }
        }catch(Exception e){
            Log.d(TAG,"Error in getting FPS: " + e.getMessage());
        }

        dm.setAvailableFpsRange(fpsString);

    }

    private void getResSize(){
        Log.d(TAG,"Available Resolution Sizes");
        try{
            resSize = param.getSupportedPreviewSizes();

            for (Camera.Size x: resSize){
                resString.add(x.width + " x " + x.height);
                Log.d(TAG,"Width: " + x.width + " Height: " + x.height);
            }
        }catch(Exception e){
            Log.d(TAG,"Error in getting Res Size: " + e.getMessage());
        }

        dm.setAvailableResSize(resString);

    }

    public void setRes(Camera.Size s){
        previewSize=s;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        count = count + 1;

        if (!dm.getImageLoadStatus()) {
            dm.loadImage(data);
        }

        // ADD THE FOLLOWING CODE TO REDUCE FPS BY HALF - FOR BT200 AND BT2000 WHERE
        // YOU CANNOT CONFIGURE FPS
        /*
        if(skip_frame){
            skip_frame = false;
        }else {
            skip_frame = true;
            if (!dm.getImageLoadStatus()) {
                dm.loadImage(data);
            }
        }
        */
    }

    public void pausePreview(){
        mCamera.stopPreview();
        mCamera.release();
    }

    public void resumePreview(){
        Log.d(TAG,"Preview Class: Resume Preview Called");
        safeCameraOpen();
        mCamera.startPreview();
    }

    public void setDataManager(DataManagement d){
        dm = d;
        dm.testDataManagerCall();
    }

    public void updateResolution(int i){
        Log.d(TAG,"Stopping camera preview to update parameters");
        mCamera.stopPreview();

        previewSize = resSize.get(i);
        param.setPreviewSize(previewSize.width,previewSize.height);
        mCamera.setParameters(param);
        dm.setImageSize(previewSize.width, previewSize.height);

        Log.d(TAG,"Restarting Preview");
        safeCameraOpen();
        try{
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
            Log.d(TAG, "Camera Preview Size: " + previewSize.width + " x " + previewSize.height);
            Log.d(TAG, "Camera FPS Range: " + fpsSelected[0] + " to " + fpsSelected[1]);
        }catch(Exception e){
            Log.d(TAG,"Error starting preview");
        }

    }

    public void updateFPS(int i){
        Log.d(TAG,"Stopping camera preview to update parameters");
        mCamera.stopPreview();

        fpsSelected = fpsList.get(i);
        param.setPreviewFpsRange(fpsSelected[0],fpsSelected[1]);
        mCamera.setParameters(param);

        Log.d(TAG,"Restarting Preview");
        safeCameraOpen();
        try{
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
            Log.d(TAG, "Camera Preview Size: " + previewSize.width + " x " + previewSize.height);
            Log.d(TAG, "Camera FPS Range: " + fpsSelected[0] + " to " + fpsSelected[1]);
        }catch(Exception e){
            Log.d(TAG,"Error starting preview");
        }

    }

}

