package com.example.user.lunchonemeal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.R.id.edit;
import static android.R.id.input;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.nhn.android.data.g.i;
import static com.nhn.android.data.g.k;

/**
 * Created by user on 2017-04-25.
 */

public class InputLocationActivity extends AppCompatActivity {


    EditText searchtext;
    Button button;
    TextView resulttext;

    XmlPullParser xpp;
    String clientId="3SnV6FqR9_YyMF7lc7pN";
    String clientSecret="ANNwdjW_w8";
    String data;

    String [] parseresult=new String[8];//타이틀 / 설명 / 구주소 / 도로명주소 / 전화번호 / 지도 x좌표 / 지도 y좌표 /링크
    // 힌트 -> String sr[]=i.getStringArrayExtra("football");

    List <ResItem> list=new ArrayList<>();

    LinearLayout mylinear;

    int index;

    final Handler handler = new Handler();







    protected void onCreate(Bundle SavedInstanceState)
    {
        super.onCreate(SavedInstanceState);


        setContentView(R.layout.inputlocationactivity);

        mylinear=(LinearLayout)findViewById(R.id.mylinear);


        searchtext=(EditText)findViewById(R.id.searchtext);
        //resulttext=(TextView)findViewById(R.id.resulttext);


        //검색버튼 이벤트
        button=(Button)findViewById(R.id.searchbtn);

        // xml값 파싱해서 ArrayList에 집어넣는 부분
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list=null;
                try {
                    //xml 파싱->array리스트 집어넣음
                    String str=new getXml().execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //쓰레드 시작
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"list크기"+list.size(),Toast.LENGTH_LONG).show();
                                mylinear.removeAllViews();
                                for(int i=0;i<list.size();i++)
                                {

                                    TextView tv=new TextView(getApplicationContext());
                                    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                                    lp.setMargins(0,0,0,15);
                                    tv.setTag(new Integer(i));
                                    tv.setLayoutParams(lp);
                                    tv.setText(list.get(i).toString());
                                    tv.setTextColor(Color.BLACK);
                                    tv.setBackgroundResource(R.drawable.edittextheme);

                                    tv.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            if((Integer)view.getTag()!=-1)
                                            {
                                                index=(Integer)view.getTag();
                                                Toast.makeText(getApplicationContext(),index+"번째",Toast.LENGTH_LONG).show();
                                            }

                                            AlertDialog.Builder alert = new AlertDialog.Builder(InputLocationActivity.this);
                                            alert.setPositiveButton("장소선택", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    Intent i=new Intent();
                                                    //타이틀 / 설명 / 구주소 / 도로명주소 / 전화번호 / 지도 x좌표 / 지도 y좌표 /링크

                                                    ResItem ri=list.get(index);
                                                    parseresult[0]=ri.getTitle();
                                                    parseresult[1]=ri.getDescription();
                                                    parseresult[2]=ri.getAddress();
                                                    parseresult[3]=ri.getRoadaddress();
                                                    parseresult[4]=ri.getTelnumber();
                                                    parseresult[5]=ri.getLongitute();
                                                    parseresult[6]=ri.getLatitude();
                                                    parseresult[7]=ri.getLink();



                                                    i.putExtra("parseResult",parseresult);

                                                    setResult(RESULT_OK,i);
                                                    finish();
                                                    dialog.dismiss();     //닫기
                                                }
                                            });
                                            alert.setNegativeButton("지도로 위치보기", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();     //닫기
                                                }
                                            });
                                            alert.setMessage("장소지정에 대한 버튼을 눌러주세요.");
                                            alert.show();


                                        }
                                    });

                                    mylinear.addView(tv);
                                    tv=null;
                                }
                                //for문 종료
                                onResume();
                            }
                        });
                    }//run문 종료
                }).start();

                new changKTMtoWgs84execute().execute((ArrayList)list);







                /*
                ResItem ri=list.get(0);
                resulttext.setText(ri.toString());
                */
            }
        });

    }

    private class settextview extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }


    private class getXml extends AsyncTask<Void,String,String> {
        @Override
        protected String doInBackground(Void... voids) {

            return getXmlData();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_LONG).show();
        }

        String getXmlData() {

            String str = searchtext.getText().toString();
            String location = "";
            try {
                location = URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String queryUrl = "https://openapi.naver.com/v1/search/local.xml?query=" + location;

            try {

                URL url = new URL(queryUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { //정상호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    publishProgress("정상");
                } else {
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    publishProgress("에러");
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                //여기까지 문자열 출력하는 부분.....이제 파싱을 시작한다..(문자열은 response.toString)

                XmlPullParserFactory xppf=XmlPullParserFactory.newInstance();
                XmlPullParser xpp=xppf.newPullParser();

                xpp.setInput(new StringReader(response.toString()));
                int eventType=xpp.getEventType();

                ResItem ri=null;

                while(eventType!=XmlPullParser.END_DOCUMENT)
                {
                    switch(eventType) {

                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case  XmlPullParser.START_DOCUMENT:
                            list = new ArrayList<ResItem>();
                            break;

                        case XmlPullParser.END_TAG: {
                            String tag = xpp.getName();
                            if(tag.equals("item"))
                            {
                                list.add(ri);
                                ri=null;
                            }

                        }

                        case XmlPullParser.START_TAG:
                            String tag=xpp.getName();
                            switch(tag) {
                                case "item":
                                    ri=new ResItem();
                                    break;

                                case "title":
                                    if(ri!=null)
                                        ri.setTitle(xpp.nextText());
                                    break;
                                case "description":
                                    if(ri!=null)
                                        ri.setDescription(xpp.nextText());
                                    break;
                                case "address":
                                    if(ri!=null)
                                        ri.setAddress(xpp.nextText());
                                    break;
                                case "roadAddress":
                                    if(ri!=null)
                                        ri.setRoadaddress(xpp.nextText());
                                    break;
                                case "telephone":
                                    if(ri!=null)
                                        ri.setTelnumber(xpp.nextText());
                                    break;
                                case "mapx":
                                    if(ri!=null)
                                        ri.setLongitute(xpp.nextText());
                                    break;
                                case "mapy":
                                    if(ri!=null)
                                        ri.setLatitude(xpp.nextText());

                                    break;
                                case "link":
                                    if(ri!=null)
                                        ri.setLink(xpp.nextText());
                                    break;
                            }
                            break;
                    }
                    eventType=xpp.next();
                }




                return response.toString();


            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    public class ResItem{


        private String title;
        private String description;
        private String address;
        private String roadaddress;
        private String telnumber;
        private String Latitude;
        private String Longitute;
        private String link;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title.replace("<b>"," ");
            this.title=this.title.replace("</b>"," ");
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description.replace("<b>"," ");
            this.description=this.description.replace("</b>"," ");
        }

        public String getLatitude() {
            return Latitude;
        }

        public void setLatitude(String Latitude) {
            this.Latitude = Latitude;
        }

        public String getLongitute() {
            return Longitute;
        }

        public void setLongitute(String Longitute) {
            this.Longitute = Longitute;
        }

        public String getRoadaddress() {
            return roadaddress;
        }

        public void setRoadaddress(String roadaddress) {
            this.roadaddress = roadaddress;
        }

        public String getTelnumber() {
            return telnumber;
        }

        public void setTelnumber(String telnumber) {
            this.telnumber = telnumber;
        }

        @Override
        public String toString() {
            return "업체명 : "+title+ "\n정보 : "+description+"\n구주소 : "+address+"\n도로명주소 : "+roadaddress+
                    "\n전화번호 : "+telnumber;
        }

        public void changKTMtoWgs84()
        {

        }
    }

    class changKTMtoWgs84execute extends AsyncTask<ArrayList<ResItem>,Void,Void>
    {
        private HashMap <String,String> map=new HashMap<String,String>();
        final String key="c0b9dcfa784cfafcf093162ef145ca11";

        @Override
        protected Void doInBackground(ArrayList<ResItem>... resitem) {

            try{

                for(int i=0;i<resitem[0].size();i++)
                {
                    ResItem ri=resitem[0].get(i);

                    URL text=new URL("http://apis.daum.net/maps/transcoord?"
                            +"apikey="+key
                            +"&x="+ri.getLongitute() //x가 경도=Longitute 동서값 큰값
                            +"&y="+ri.getLatitude() //y가 위도=Latitude 남북값 작은값
                            +"&fromCoord=KTM"
                            +"&toCoord=WGS84"
                            +"&output=xml");


                    //파싱 시작
                    XmlPullParserFactory parserCreator=XmlPullParserFactory.newInstance();
                    XmlPullParser parser=parserCreator.newPullParser();

                    parser.setInput(text.openStream(),null);
                    int parserEvent=parser.getEventType();

                    while(parserEvent!=XmlPullParser.END_DOCUMENT)
                    {
                        switch (parserEvent) {
                            case XmlPullParser.START_TAG:
                                String tag = parser.getName();
                                if (tag.compareTo("result") == 0) {


                                    ri.setLongitute(parser.getAttributeValue(null, "x"));
                                    ri.setLatitude(parser.getAttributeValue(null, "y"));

                                    //map.put("longitude", parser.getAttributeValue(null, "x"));
                                    //map.put("latitude", parser.getAttributeValue(null, "y"));

                                }
                                break;
                        }
                        parserEvent = parser.next();
                    }

                }


            }catch(Exception e)
            {
                Log.e("Net","Error in network call",e);

            }

            return null;
        }
    }




}
