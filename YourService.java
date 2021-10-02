package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import gov.nasa.arc.astrobee.Kinematics;
import gov.nasa.arc.astrobee.types.Vec3d;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import com.google.zxing.*;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.common.HybridBinarizer;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.aruco.*;
import org.opencv.core.MatOfFloat;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.nio.ByteBuffer;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {

    static String TAG="YourService";
    String[] answer = new String[6];
    Bitmap[][] BigAlbum = new Bitmap[6][6];
    Boolean[] SignalA = {false,false,false,false,false,false};
    Boolean[] SignalB = {false,false,false,false,false,false};
    Boolean[] SignalC = {false,false,false,false,false,false};
    Point[] Road = {new Point(11.45, -5.7, 4.55),new Point(11, -5.5, 4.4),new Point(11, -6, 5.45),
            new Point(10.45, -6.45, 5.45),new Point(10.7, -6.9, 5.45),new Point(11.1, -6.9, 5.45),
            new Point(11, -7.7, 5.45),new Point(10.45, -7.5, 4.7),new Point(11.45, -8, 5)};
    Quaternion[] RoadQScan = {new Quaternion(0,0, 1, 0),new Quaternion((float)-0.5,(float)-0.5,(float)-0.5,(float)0.5),new Quaternion((float)-0.5, (float)-0.5,(float) -0.5, (float)0.5),
            new Quaternion((float)0, (float)0, (float)-0.7071068, (float)0.7071068),new Quaternion(0, 0, 0, 1),new Quaternion(0, 0, 0, 1),
            new Quaternion((float)-0.7071068, (float)0, (float)0.7071068, 0),new Quaternion(0, 0, 0, 1),new Quaternion(0, 0, 0, 1)};
    Quaternion[] RoadQMoveFront = {new Quaternion(0,0, 1, 0),new Quaternion((float)-0.5,(float)-0.5,(float)-0.5,(float)0.5),new Quaternion((float)-0.5, (float)-0.5,(float) -0.5, (float)0.5),
            new Quaternion((float)0, (float)0, (float)-0.7071068, (float)0.7071068),new Quaternion(0, 0, 0, 1),new Quaternion(0, 0, 0, 1),
            new Quaternion((float)-0.7071068, (float)0, (float)0.7071068, 0),new Quaternion(0, 0, 0, 1),new Quaternion(0, 0, 0, 1)};

    Quaternion[] RoadQMoveRear = {new Quaternion(0,0, 1, 0),new Quaternion(0,0,0,1),new Quaternion((float)-0.5, (float)0.5,(float) 0.5, (float)0.5),
            new Quaternion((float)0, (float)0, (float)0.7071068, (float)0.7071068),new Quaternion((float)0, (float)0, (float)0.7071068, (float)0.7071068),new Quaternion(0, 0, 1, 0),
            new Quaternion(0,0,0,1),new Quaternion(0, 0, 0, 1),new Quaternion(0, 0, 0, 1)};
    Boolean[] IsFrontCamera =  {false,false,true,false,false,true};
    int pointer = -1;
    int[] Location = {0,1,2,6,7,8};

    @Override
    protected void runPlan1(){
        api.judgeSendStart();

        //moveToWrapper(11.45, -5.7, 4.55, 0,0, 0, 1);
        // initializeZxing();

        moveToWrapper(11.45, -5.7, 4.55, 0,0, 1, 0); //Go Scan P1-1
        pointer++;
        BigAlbum[0]= BackCamera(11.45, -5.7, 4.55, 0,0, 1, 0);
        SignalA[0]=true;
        AnotherThread anotherthread= new AnotherThread(this);
        anotherthread.start();

        moveToWrapper(11, -5.5, 4.4, -0.5,-0.5,-0.5,0.5); //Go Scan P1-3
        pointer++;
        BigAlbum[1]= BackCamera(11, -5.5, 4.4, -0.5,-0.5,-0.5,0.5);
        SignalA[1]=true;
        if(!anotherthread.IsWakeUp) anotherthread.wake();
        QRSend();

        moveToWrapper(11, -6, 5.45, -0.5, -0.5, -0.5, 0.5); //Go Scan P1-2
        pointer++;
        BigAlbum[2]= Camera(11, -6, 5.45, -0.5, -0.5, -0.5, 0.5);
        SignalA[2]=true;
        if(!anotherthread.IsWakeUp) anotherthread.wake();
        QRSend();

        moveToWrapper(10.45, -6.45, 5.45, 0, 0, -0.7071068, 0.7071068); //Move to A
        pointer++;
        QRSend();
        moveToWrapper(10.7, -6.9, 5.45, 0, 0, 0, 1); //Move to B
        pointer++;
        QRSend();
        moveToWrapper(11.1, -6.9, 5.45, 0, 0, 0, 1); //Move to C
        pointer++;
        QRSend();

        moveToWrapper(11, -7.7, 5.45, -0.7071068, 0, 0.7071068, 0); //Go Scan P2-3
        pointer++;
        BigAlbum[3]= BackCamera(11, -7.7, 5.45, -0.7071068, 0, 0.7071068, 0);
        SignalA[3]=true;
        if(!anotherthread.IsWakeUp) anotherthread.wake();
        QRSend();

        moveToWrapper(10.45, -7.5, 4.7, 0, 0, 0, 1); //Go Scan P2-1
        pointer++;
        BigAlbum[4]= BackCamera(10.45, -7.5, 4.7, 0, 0, 0, 1);
        SignalA[4]=true;
        if(!anotherthread.IsWakeUp) anotherthread.wake();
        QRSend();

        moveToWrapper(11.45, -8, 5, 0, 0, 0, 1); //Go Scan P2-2
        pointer++;
        BigAlbum[5]= Camera(11.45, -8, 5, 0, 0, 0, 1);
        SignalA[5]=true;
        if(!anotherthread.IsWakeUp) anotherthread.wake();
        QRSend();

        moveToWrapper(10.95, -8.55, 4.7, 0, 0, -0.7071068, 0.7071068); //Move to E
        moveToWrapper(10.95, -9.25, 4.7, 0, 0, -0.7071068, 0.7071068); //Move to F

        for(int i=0;i<6;i++){
            for(int t=0;t<1200&&!SignalB[i]&&!SignalC[i];t++){
                Log.d(TAG,"Number "+i+"has not finished,wait it for time:"+t);
                sleep(500);
            } //Waiting
                if(SignalB[i]&&!SignalC[i]){
                    if(answer[i]=="Failed."){
                        Log.d(TAG,"No"+i+"Correction");
                        for(int t=0;t<6;t++){
                            BigAlbum[i][t].recycle();
                        } //Recycling
                        Correction(i);
                        SignalB[i]=false;
                    }else{ //Correction
                    for(int t=0;t<6;t++){
                        BigAlbum[i][t].recycle();
                    } //Recycling
                    switch(i){
                        case 0:
                            Log.d(TAG,"QR0:"+answer[i]);
                            api.judgeSendDiscoveredQR(0,answer[i]);
                            break;
                        case 1:
                            Log.d(TAG,"QR2:"+answer[i]);
                            api.judgeSendDiscoveredQR(2,answer[i]);
                            break;
                        case 2:
                            Log.d(TAG,"QR1:"+answer[i]);
                            api.judgeSendDiscoveredQR(1,answer[i]);
                            break;
                        case 3:
                            Log.d(TAG,"QR5:"+answer[i]);
                            api.judgeSendDiscoveredQR(5,answer[i]);
                            break;
                        case 4:
                            Log.d(TAG,"QR3:"+answer[i]);
                            api.judgeSendDiscoveredQR(3,answer[i]);
                            break;
                        case 5:
                            Log.d(TAG,"QR4:"+answer[i]);
                            api.judgeSendDiscoveredQR(4,answer[i]);
                            break;
                    }
                    SignalC[i]=true;
                }
            }
        } //Sending QR With waiting

        double pos_x = Double.parseDouble(answer[0].substring(7));
        double pos_y = Double.parseDouble(answer[2].substring(7));
        double pos_z = Double.parseDouble(answer[1].substring(7));
        double qua_x = Double.parseDouble(answer[4].substring(7));
        double qua_y = Double.parseDouble(answer[5].substring(7));
        double qua_z = Double.parseDouble(answer[3].substring(7));
        double qua_w = Math.sqrt(1-(qua_x*qua_x+qua_y*qua_y+qua_z*qua_z));
        Log.d(TAG,"pos_x "+pos_x+" pos_y "+pos_y+" pos_z "+pos_z+" qua_x "+qua_x+" qua_y "+qua_y+" qua_z "+qua_z+" qua_w "+qua_w);
        moveToWrapper(pos_x,pos_y,pos_z,0,0,-0.7071068,0.7071068);

        Mat luka = api.getMatNavCam();
        Mat ids = new Mat();
        List<Mat> C = new ArrayList<>();
        Aruco.detectMarkers(luka,Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250),C,ids);
        for(int i=0;i<3&&ids.empty();i++){
            Point CP = api.getTrustedRobotKinematics().getPosition();
            if( Math.abs(CP.getX()-pos_x)>=0.05 || Math.abs(CP.getY()-pos_y) >=0.05 || Math.abs(CP.getZ()-pos_z) >= 0.05 ) moveToWrapper(pos_x, pos_y, pos_z, 0,0,-0.7071068,0.7071068);
            luka=api.getMatNavCam();
            Aruco.detectMarkers(luka,Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250),C,ids);
            Log.d(TAG,"aruco failed "+i+" time");
        } //3 Chances to Scan AR
        if(ids.empty()){
            moveToWrapper(10.95,pos_y,4.55,0,0,-0.7071068,0.7071068);
            for(int i=0;i<3&&ids.empty();i++){
                Point CP = api.getTrustedRobotKinematics().getPosition();
                if( Math.abs(CP.getX()-pos_x)>=0.05 || Math.abs(CP.getY()-pos_y) >=0.05 || Math.abs(CP.getZ()-pos_z) >= 0.05 ) moveToWrapper(10.95, pos_y, 4.85, 0,0,-0.7071068,0.7071068);
                luka=api.getMatNavCam();
                Aruco.detectMarkers(luka,Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250),C,ids);
                Log.d(TAG,"aruco failed "+i+" time");
            }
        } //go to the center and Scan
        if(ids.empty()){
            moveToWrapper(pos_x,pos_y,pos_z,0,0,-0.7071068,0.7071068);
            api.laserControl(true);
            api.judgeSendFinishSimulation();
        }//Go to P3 and Laser
        int arid = (int)(ids.get(0,0)[0]);
        String ARID = Integer.toString(arid);
        api.judgeSendDiscoveredAR(ARID);
        Log.d(TAG,ARID);

        Mat CMM= new Mat(3,3, CvType.CV_64F);
        double[][] CMF = {{344.173397, 0.000000, 630.793795}, {0.000000, 344.277922, 487.033834}, {0.000000, 0.000000, 1.000000}};
        for(int i=0;i<3;i++){
            for(int t=0;t<3;t++){
                CMM.put(i,t,CMF[i][t]);
            }
        }
        Mat DCM = new Mat(1,5,CvType.CV_64F);
        double[] DCF ={-0.152963, 0.017530, -0.001107, -0.000210, 0.000000};
        for(int i=0;i<5;i++) DCM.put(0,i,DCF[i]);
        Mat rvecs = new Mat();
        Mat tvecs = new Mat();
        Aruco.estimatePoseSingleMarkers(C,  (float)0.05,CMM,DCM,rvecs,tvecs);
        double miku_x = pos_x+(tvecs.get(0,0)[0]+0.02);
        double miku_y = pos_y;
        double miku_z = pos_z+(tvecs.get(0,0)[1]+0.25);
        Log.d(TAG,"Moveto: "+miku_x+","+pos_y+","+miku_z);
        Point CP = api.getTrustedRobotKinematics().getPosition();
        if( Math.sqrt(Math.pow(tvecs.get(0,0)[0]+0.02,2)+Math.pow(tvecs.get(0,0)[1]+0.25,2))<=0.1) moveToWrapper(10.95, -9.25, 4.7,0, 0, -0.7071068, 0.7071068);
        moveToWrapper(miku_x,miku_y,miku_z,0,0,-0.7071068,0.7071068);
        /*for(int i=0;i<10;i++){
            luka=api.getMatNavCam();
            Aruco.detectMarkers(luka,Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250),C,ids);
            Aruco.estimatePoseSingleMarkers(C,  (float)0.05,CMM,DCM,rvecs,tvecs);
            Point CP = api.getTrustedRobotKinematics().getPosition();
            Log.d(TAG,"Laser "+i+" Moveto: "+(pos_x+(tvecs.get(0,0)[0]-0.05))+","+pos_y+","+(pos_z+(tvecs.get(0,0)[1]+0.8)));
            moveToWrapper(CP.getX()+(tvecs.get(0,0)[0]-0.05),pos_y,CP.getZ()+(tvecs.get(0,0)[1]+0.8),0,0,-0.7071068,0.7071068);
            api.laserControl(true);
            api.laserControl(false);
        }*/
        sleep(200);
        api.laserControl(true);
        api.judgeSendFinishSimulation();
    }

    @Override
    protected void runPlan2(){
    }

    @Override
    protected void runPlan3(){
    }

    // You can add your method
    public void moveToWrapper(double pos_x, double pos_y, double pos_z,
                              double qua_x, double qua_y, double qua_z,
                              double qua_w){
        final int a = 10;
        final Point point = new Point(pos_x, pos_y, pos_z);
        final Quaternion quaternion = new Quaternion((float)qua_x, (float)qua_y,
                (float)qua_z, (float)qua_w);

        Result result = api.moveTo(point, quaternion, true);
        for(int i=0;i<3&&!result.hasSucceeded();i++){
            sleep(200);
            result = api.moveTo(point, quaternion, true);
            Log.d(TAG,"I fly fail.");
        }
    }
    public void moveToWrapper(Point point,Quaternion quaternion){
        Result result = api.moveTo(point, quaternion, true);
        for(int i=0;i<3&&!result.hasSucceeded();i++){
            sleep(200);
            result = api.moveTo(point, quaternion, true);
            Log.d(TAG,"I fly fail.");
        }
    }
    public void sleep(int ms){
        try{
            Thread.sleep(ms);
        }catch(Exception e){
            Log.d(TAG,"I was sleeping.");
        }
    }

    public Bitmap[] Camera(double pos_x, double pos_y, double pos_z,
                                double qua_x, double qua_y, double qua_z,
                                double qua_w){
        Bitmap[] album = new Bitmap[6];
        for(int n=0;n<6;n++){
            Point CP = api.getTrustedRobotKinematics().getPosition();
            if( Math.abs(CP.getX()-pos_x)>=0.05 || Math.abs(CP.getY()-pos_y) >=0.05 || Math.abs(CP.getZ()-pos_z) >= 0.05 ) moveToWrapper(pos_x, pos_y, pos_z, qua_x, qua_y, qua_z, qua_w);
            sleep(500);
            album[n]=api.getBitmapNavCam();
        }
        return album;
    }
    public Bitmap[] Camera(Point point,Quaternion quaternion){
        Bitmap[] album = new Bitmap[6];
        for(int n=0;n<6;n++){
            Point CP = api.getTrustedRobotKinematics().getPosition();
            if( Math.abs(CP.getX()-point.getX())>=0.05 || Math.abs(CP.getY()-point.getY()) >=0.05 || Math.abs(CP.getZ()-point.getZ()) >= 0.05 ) moveToWrapper(point,quaternion);
            sleep(500);
            album[n]=api.getBitmapNavCam();
        }
        return album;
    }
    public Bitmap[] BackCamera(Point point,Quaternion quaternion){
        Bitmap[] album = new Bitmap[6];
        for(int n=0;n<6;n++){
            Point CP = api.getTrustedRobotKinematics().getPosition();
            if( Math.abs(CP.getX()-point.getX())>=0.05 || Math.abs(CP.getY()-point.getY()) >=0.05 || Math.abs(CP.getZ()-point.getZ()) >= 0.05 ) moveToWrapper(point,quaternion);
            sleep(500);
            album[n]=api.getBitmapDockCam();
        }
        return album;
    }
    public Bitmap[] BackCamera(double pos_x, double pos_y, double pos_z,
                           double qua_x, double qua_y, double qua_z,
                           double qua_w){
        Bitmap[] album = new Bitmap[6];
        for(int n=0;n<6;n++){
            Point CP = api.getTrustedRobotKinematics().getPosition();
            if( Math.abs(CP.getX()-pos_x)>=0.05 || Math.abs(CP.getY()-pos_y) >=0.05 || Math.abs(CP.getZ()-pos_z) >= 0.05 ) moveToWrapper(pos_x, pos_y, pos_z, qua_x, qua_y, qua_z, qua_w);
            sleep(500);
            album[n]=api.getBitmapDockCam();
        }
        return album;
    }

    public void QRSend(){
        for(int i=0;i<6;i++){
            if(SignalB[i]&&!SignalC[i]){
                if(answer[i]=="Failed."){
                    Log.d(TAG,"No"+i+"Correction");
                    for(int t=0;t<6;t++){
                        BigAlbum[i][t].recycle();
                    }
                    Correction(i);
                    SignalB[i]=false;
                }else{
                    for(int t=0;t<6;t++){
                        BigAlbum[i][t].recycle();
                    }
                    switch(i){
                        case 0:
                            Log.d(TAG,"QR0:"+answer[i]);
                            api.judgeSendDiscoveredQR(0,answer[i]);
                            break;
                        case 1:
                            Log.d(TAG,"QR2:"+answer[i]);
                            api.judgeSendDiscoveredQR(2,answer[i]);
                            break;
                        case 2:
                            Log.d(TAG,"QR1:"+answer[i]);
                            api.judgeSendDiscoveredQR(1,answer[i]);
                            break;
                        case 3:
                            Log.d(TAG,"QR5:"+answer[i]);
                            api.judgeSendDiscoveredQR(5,answer[i]);
                            break;
                        case 4:
                            Log.d(TAG,"QR3:"+answer[i]);
                            api.judgeSendDiscoveredQR(3,answer[i]);
                            break;
                        case 5:
                            Log.d(TAG,"QR4:"+answer[i]);
                            api.judgeSendDiscoveredQR(4,answer[i]);
                            break;
                    }
                    SignalC[i]=true;
                }


            }
        }
    }
    public void Correction(int i){
        int goal = Location[i];
        int OP = pointer;
        Log.d(TAG,"From "+ OP + " to "+goal);
        for(;pointer>goal+1;pointer--){
            moveToWrapper(Road[pointer-1],RoadQMoveRear[pointer-1]);
        }
        moveToWrapper(Road[pointer-1],RoadQScan[pointer-1]);
        pointer--;
        if(IsFrontCamera[i]) BigAlbum[i]=Camera(Road[goal],RoadQScan[goal]);
            else BigAlbum[i] = BackCamera(Road[goal],RoadQScan[goal]);
        SmallThread smallthread= new SmallThread(this,i);
        smallthread.start();
        for(;pointer<OP;pointer++){
            moveToWrapper(Road[pointer+1],RoadQMoveFront[pointer]);
        }
    }
}