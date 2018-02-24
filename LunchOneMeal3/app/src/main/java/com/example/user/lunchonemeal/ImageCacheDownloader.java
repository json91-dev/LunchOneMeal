package com.example.user.lunchonemeal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * Created by user on 2017-05-13.
 */

public class ImageCacheDownloader extends AsyncTask<String,Integer,Bitmap> {
    private Context context=null;
    private ImageView imageview=null;
    private String imageURL=null;

    public ImageCacheDownloader(Context _context, ImageView _imageview,String _url)
    {
        this.context=_context;
        this.imageview=_imageview;
        this.imageURL=_url;
    }

    private String urlToFileFullPath(String _url)
    {
        return context.getCacheDir().getAbsolutePath()+_url.substring(_url.lastIndexOf("/"),_url.length());
    }

    protected void onPreExecute()
    {
        super.onPreExecute();
        String fileFullPath=urlToFileFullPath(imageURL);
        if(new File(fileFullPath).exists())
        {
            //파일이 존재하면, 저장되어있는 이미지를 화면에 표시한다.
            Bitmap myBitmap= BitmapFactory.decodeFile(fileFullPath);
            imageview.setImageBitmap(myBitmap);
        }
    }

    protected void onPostExecute(Bitmap result)
    {
        super.onPostExecute(result);

        String fileFullPath=urlToFileFullPath(imageURL);//새로운 파일
        String tempFilePath=fileFullPath+"_temp";//새로운 파일의 임시파일 경로

        writeFile(result,new File(tempFilePath));//임시파일 생성
        File downTempFile=new File(tempFilePath);//임시파일 포인터
        File newFile=new File(fileFullPath);//새로운 파일 포인터

        if(new File(fileFullPath).exists())
        {
            //파일이 있으면

            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize=16;

            Bitmap prevBitmap=BitmapFactory.decodeFile(fileFullPath,option);//이전의 파일의 비트맵
            Bitmap downBitmap=BitmapFactory.decodeFile(downTempFile.getAbsolutePath(),option);//새로운 파일의 비트맵

            if(sameAs(prevBitmap,downBitmap))
            {
                Log.i("egg","같은 사진임");
            }
            else{
                Log.i("egg","다른 사진이라서 새로 설정한다");
                imageview.setImageBitmap(result);
                writeFile(result,newFile);
            }
        }else{
            writeFile(result,newFile);
            imageview.setImageBitmap(result);
        }
        Toast.makeText(context,fileFullPath,Toast.LENGTH_LONG).show();
        downTempFile.delete();
    }

    private boolean sameAs(Bitmap bitmap1,Bitmap bitmap2)
    {
        ByteBuffer buffer1=ByteBuffer.allocate(bitmap1.getHeight()*bitmap1.getRowBytes());
        bitmap1.copyPixelsToBuffer(buffer1);

        ByteBuffer buffer2=ByteBuffer.allocate(bitmap2.getHeight()*bitmap2.getRowBytes());
        bitmap2.copyPixelsToBuffer(buffer2);

        return Arrays.equals(buffer1.array(),buffer2.array());
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        return downloadBitmap(imageURL);
    }

    //새로운 파일을 쓴다.
    private void writeFile(Bitmap bmp,File f)
    {
        FileOutputStream out=null;
        try{
            out=new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG,50,out);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            try{
                if(out!=null)
                    out.close();
            }catch(Exception e){

            }
        }
    }

    private Bitmap downloadBitmap(String imageUrl)
    {
        Bitmap bm=null;
        try{

            URL url=new URL(imageUrl);
            URLConnection urlc=url.openConnection();
            BufferedInputStream bis=new BufferedInputStream(urlc.getInputStream());
            bm= BitmapFactory.decodeStream(bis);
            bis.close();
            return bm;
        }catch (Exception e) {
            e.printStackTrace();
        }

        return bm;
    }

}
