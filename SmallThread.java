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

public class SmallThread extends Thread {

    static String TAG="YourService";
    private Bitmap[][] album =new Bitmap[6][6];
    private jp.jaxa.iss.kibo.rpc.sampleapk.YourService YS;
    public boolean IsWakeUp = true;
    private int no;

    public SmallThread(jp.jaxa.iss.kibo.rpc.sampleapk.YourService YS,int no){
        this.YS = YS;
        this.no=no;
    }

    public void run(){
        Log.d(TAG,"ST Start");
        String result = Scan(YS.BigAlbum[no][0]);
        for(int i=1;i<6&&result=="Failed.";i++){
            result = Scan(YS.BigAlbum[no][i]);
        }
        if(result=="Failed."){
            Log.d(TAG,"Number"+no+"Failed.");

        }
        YS.answer[no]=result;
        YS.SignalB[no]=true;
        Log.d(TAG,"ST End");

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
}
