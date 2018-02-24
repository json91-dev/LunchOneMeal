package com.example.user.lunchonemeal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class ImageDownloader extends AsyncTask<String,Integer,Bitmap> {
    private Context context=null;
    private ImageView imageview=null;
    private String imageURL=null;

    public ImageDownloader(Context _context, ImageView _imageview, String _url)
    {
        this.context=_context;
        this.imageview=_imageview;
        this.imageURL=_url;
    }

    private String urlToFileFullPath(String _url)
    {
        return context.getCacheDir().getAbsolutePath()+_url.substring(_url.lastIndexOf("/"),_url.length());
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
