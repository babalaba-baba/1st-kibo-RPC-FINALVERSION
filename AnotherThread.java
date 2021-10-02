package jp.jaxa.iss.kibo.rpc.sampleapk;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

public class AnotherThread extends Thread {

    static String TAG="YourService";
    private Bitmap[][] album =new Bitmap[6][6];
    private jp.jaxa.iss.kibo.rpc.sampleapk.YourService YS;
    public boolean IsWakeUp = true;

    public AnotherThread(jp.jaxa.iss.kibo.rpc.sampleapk.YourService YS){
        this.YS = YS;
    }

    public void run(){
        for(int t=0;t<6;t++){
            if(!YS.SignalA[t]){
                IsWakeUp = false;
                halt(t);
            }
            IsWakeUp = true;
            String result = Scan(YS.BigAlbum[t][0]);
            for(int i=1;i<6&&result=="Failed.";i++){
                result = Scan(YS.BigAlbum[t][i]);
                //Log.d(TAG,"Result:"+result);
            }
            if(result=="Failed."){
                Log.d(TAG,"Number"+t+"Failed.");

            }
            YS.answer[t]=result;
            YS.SignalB[t]=true;
        }

    }

    public String Scan(Bitmap rin){
        //Log.d(TAG,"intArray");
        int[] intArray = new int[rin.getWidth()*rin.getHeight()];
        //Log.d(TAG,"wrap");
        IntBuffer IBF = IntBuffer.wrap(intArray);
        //Log.d(TAG,"toBuffer");
        rin.copyPixelsToBuffer(IBF);
        //Log.d(TAG,"RGB");
        LuminanceSource LS = new RGBLuminanceSource(rin.getWidth(),rin.getHeight(), intArray);
        //Log.d(TAG,"BBM");
        BinaryBitmap BBM = new BinaryBitmap(new HybridBinarizer(LS));
        Map hints = new HashMap();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        try{
            //Log.d(TAG,"getResult");
            com.google.zxing.Result R = new MultiFormatReader().decode(BBM,hints);
            return R.getText();
        }catch(NotFoundException e){
            return "Failed.";
        }
    }
    public synchronized void halt(int t){
        try {
            //Log.d(TAG,"Time : "+t+"waiting.");
            wait();
        } catch (InterruptedException e) {
            //Log.d(TAG,"I was waiting.");
        }
    }
    public synchronized void wake() {
        notify();
    }
}
