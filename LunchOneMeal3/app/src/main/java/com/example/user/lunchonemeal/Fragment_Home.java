package com.example.user.lunchonemeal;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static android.R.attr.endX;
import static android.R.attr.name;
import static android.R.id.edit;
import static android.R.string.no;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

import static android.os.Build.VERSION_CODES.N;
import static com.example.user.lunchonemeal.R.drawable.location;
import static com.example.user.lunchonemeal.R.id.detail;
import static com.example.user.lunchonemeal.R.id.distance;
import static com.example.user.lunchonemeal.R.id.mList;
import static com.example.user.lunchonemeal.R.id.notification_main_column;
import static com.example.user.lunchonemeal.R.id.start;
import static com.nhn.android.data.g.a;
import static com.nhn.android.data.g.g;
import static com.nhn.android.data.g.i;
import static com.nhn.android.data.g.j;

/**
 * Created by user on 2017-04-18.
 */

public class Fragment_Home extends Fragment implements /*NMapView.OnMapStateChangeListener,*/TabHost.OnTabChangeListener/*,NMapPOIdataOverlay.OnStateChangeListener*/{

    private ListView mListView=null;
    private ListViewAdapter mAdapter=null;
    public String myJSON;
    JSONArray peoples=null;
    private  static final String TAG_RESULT="result";
    private  static final String TAG_TITLE="title";
    private static final String TAG_PRICE="price";
    private static final String TAG_NAME="name";
    private static final String TAG_URL="url";
    private static final String TAG_INFO="information";
    private static final String TAG_NO="no";
    private static final String TAG_LNG="longitude";
    private static final String TAG_LAT="latitude";

    String tabIndex="tag1";

    //위도 경도 얻기
    private LocationManager locationManager;
    private Location mLocation;

    //지도 표시



    private static final String CLIENT_ID="3SnV6FqR9_YyMF7lc7pN";

    //NMapController mMapController;
    //NMapView mapView;
    //NMapViewerResourceProvider mMapViewResourceProvider;
    //NMapOverlayManager mOverlayManager;
    //NMapPOIdataOverlay poiDataOverlay;
    //private NMapContext mMapContext;

    Handler myhandler;


    public LocationListener locationListener;

    ArrayList<LocationData> locationdata=new ArrayList<>();

    Button search;
    Button search2;
    ProgressDialog loading;

    Spinner kind,distance;
    ImageButton locationButton;


    //지도 tab의 변수들

    Spinner kind2,distance2;
    ImageButton locationButton2;
    boolean LocationButton2Flag=false;



    //구글지도 변수 설정
    private GoogleMap googlemap=null;
    private MapView mapView=null;

    static final LatLng SEOUL = new LatLng( 37.56, 126.97);

    //googlemap 말풍선의 imageview
    ImageView image_;



    //클러스터 아이템을 관리하는 객체 생성
    //private ClusterManager<MyItem> mClusterManager;


    //InfoWindow에서 클릭 이벤트 발생시 특점 지점의 상세정보를 가져오기 위해 사용할 Intent의 No값
    String detailNo=null;

    //도보 경로 안내 리스트뷰 변수설정

    ListView road_listview;
    Road_ListViewAdapter road_adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        Toast.makeText(getActivity(),"onCreate 호출 ",Toast.LENGTH_LONG).show();



        //1. 지도의 권한을 얻어옴
        //mMapContext=new NMapContext(super.getActivity());
        //mMapContext.onCreate();

        //위치 관리자의 권한을 얻어옴
        locationManager=(LocationManager)getActivity().getSystemService(getActivity().LOCATION_SERVICE);

        //gps나 네트워크가 사용 가능한 상태인지 알아본다
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);



        if(isNetworkEnabled){
            Log.e("GPS Enable","true");

            final List<String> m_lstProviders = locationManager.getProviders(false);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    if(getActivity()!=null) {

                        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        }

                        Toast.makeText(getActivity(), "위치 -> 현재위치로 저장", Toast.LENGTH_LONG).show();

                        //바뀐 위치를 mLocation에 저장한다.
                        //위치 리스너가 실행될때 출력
                        mLocation = location;





                        Log.e("onLocationChanged", "onLocationChanged");
                        Log.e("location", "[" + location.getProvider() + "] (" + location.getLatitude() + "," + location.getLongitude() + ")");
                        locationManager.removeUpdates(locationListener);


                        //showList는 위치 리스너가 실행 되어야지만 실행된다. 따라서 거리 오류가 없을 수 밖에 없다.
                        if(LocationButton2Flag==false) {
                            //2번쨰 버튼 플래그가 꺼져있으면
                            //showList()실행
                            showList();
                        }
                        else
                        {

                            //위치정보 (mapget.php) 로 값을 가져와서 locationdata(class)에 값을 입력시킨다.
                            //시작될때 mapget.php에서 정보를 가져와서 값입력
                            new getlocationdata().execute();
                            //2번쨰 버튼 플래그가 켜져있으면
                            //지도에서 현재 위치를 찾아서 이동
                            LatLng latlng=new LatLng(location.getLatitude(),location.getLongitude());
                            googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,16));

                        }

                        LocationButton2Flag=false;
                        loading.dismiss();



                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                    Log.e("onStatusChanged", "onStatusChanged");
                }

                @Override
                public void onProviderEnabled(String s) {
                    Log.e("onProviderEnabled", "onProviderEnabled");
                }

                @Override
                public void onProviderDisabled(String s) {
                    Log.e("onProviderDisabled", "onProviderDisabled");
                }
            };

            Toast.makeText(getActivity(),"Fragment_Home OnCreate() 호출 ",Toast.LENGTH_LONG).show();
            //onCreate될때 지역 정보를 받아옴.

        }
    }



    /*
    @Override
    public void onZoomLevelChange(NMapView nMapView, int i) {

    }
    */

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //mapView.setClickable(true);
        //mapView.setClientId(CLIENT_ID);
        //mMapContext.setupMapView(mapView);
        //mMapController =mapView.getMapController();

        if(mapView!=null)
        {
            mapView.onCreate(savedInstanceState);
        }

    }
    @Override
    public void onStart() {
        //mMapContext.onStart();
        //mapView.setOnMapStateChangeListener(this);
        //mapView.setBuiltInZoomControls(true,null);
        //리소스 프로바이더 설정
        //mMapViewResourceProvider=new NMapViewerResourceProvider(getActivity());
        //mOverlayManager=new NMapOverlayManager(getActivity(),mapView,mMapViewResourceProvider);;

        //구글맵 onstart();
        mapView.onStart();

        super.onStart();

    }
    /*
    //리스너 등록하기
    @Override
    public void onAnimationStateChange(NMapView nMapView, int i, int i1) {
    }
    @Override
    public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {

    }
    @Override
    public void onMapCenterChangeFine(NMapView nMapView) {

    }
    */

    //위도 경도 얻어오기
    /*
    @Override
    public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {
        if (nMapError == null) { // success

            Toast.makeText(getActivity(),"지도 초기화",Toast.LENGTH_LONG).show();
            //mapView.setBuiltInAppControl(true);

        } else { // fail
            Log.e("지도 초기화 에러발생", "onMapInitHandler: error=" + nMapError.toString());
            Toast.makeText(getActivity(),nMapView.toString()+"",Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onCalloutClick(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
        Toast.makeText(getContext(), "onCalloutClick: " + nMapPOIitem.getId(), Toast.LENGTH_LONG).show();
    }
    @Override
    public void onFocusChanged(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
    }
    */

    @Override
    public void onStop() {

        super.onStop();
        //구글맵 onStop();
        mapView.onStop();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    ///////////////////////이부분부터는 리스트뷰 관련된 내용 /// 윗부분은 네이버 지도 관련된 내용/,,,,

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.fragment_home,container,false);


        TabHost tabHost=(TabHost)rootView.findViewById(R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec spec=tabHost.newTabSpec("tag1");
        spec.setContent(R.id.tv1);
        spec.setIndicator("주변한끼");
        tabHost.addTab(spec);

        spec=tabHost.newTabSpec("tag2");
        spec.setContent(R.id.tv2);
        spec.setIndicator("지도");
        tabHost.addTab(spec);

        /*
        spec=tabHost.newTabSpec("tag3");
        spec.setContent(R.id.tv3);
        spec.setIndicator("대화방");
        tabHost.addTab(spec);
        */

        kind=(Spinner)rootView.findViewById(R.id.kindSpinner);
        kind2=(Spinner)rootView.findViewById(R.id.kindSpinner2);
        distance=(Spinner)rootView.findViewById(R.id.DistanceSpinner);
        distance2=(Spinner)rootView.findViewById(R.id.DistanceSpinner2);

        tabHost.setCurrentTab(0);
        tabHost.setOnTabChangedListener(this);

        locationButton=(ImageButton)rootView.findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            myhandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    loading=ProgressDialog.show(getActivity(),"Loading...",
                                            "Please Wait...",false,false);

                                    //String [] a={"NETWORK","NETWORK","NETWORK"};

                                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                                    }
                                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

                                }
                            });
                        }
                    }).start();
                }
            }
        });

        search=(Button)rootView.findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String kind_,distance_;
                if(kind.getSelectedItem().equals("분류"))
                    kind_="all";
                else if(kind.getSelectedItem().equals("식사"))
                    kind_="mea";
                else if(kind.getSelectedItem().equals("디저트"))
                    kind_="des";
                else
                    kind_="caf";


                if(distance.getSelectedItem().equals("거리"))
                    distance_="0";
                else if(distance.getSelectedItem().equals("1KM"))
                    distance_="1";
                else if(distance.getSelectedItem().equals("3KM"))
                    distance_="3";
                else
                    distance_="5";

                String filterText =kind_+distance_;

                Toast.makeText(getActivity(),filterText+"",Toast.LENGTH_LONG).show();
                /*
                if (filterText.length() > 0)
                {
                    mListView.setFilterText(filterText) ;
                }
                else {
                    mListView.clearTextFilter();
                }
                */
                ((ListViewAdapter)mListView.getAdapter()).getFilter().filter(filterText) ;
            }
        });

        search2=(Button)rootView.findViewById(R.id.search2);
        search2.setOnClickListener(new View.OnClickListener() {

            //search버튼을 눌렀을때
            @Override
            public void onClick(View view) {
                String kind_,distance_;
                //스피너에서 분류를 위한 값을 가져온다.

                if(kind2.getSelectedItem().equals("분류"))
                    kind_="all";
                else if(kind2.getSelectedItem().equals("식사"))
                    kind_="mea";
                else if(kind2.getSelectedItem().equals("디저트"))
                    kind_="des";
                else
                    kind_="caf";


                if(distance2.getSelectedItem().equals("거리"))
                    distance_="0";
                else if(distance2.getSelectedItem().equals("1KM"))
                    distance_="1";
                else if(distance2.getSelectedItem().equals("3KM"))
                    distance_="3";
                else
                    distance_="5";

                //markerset함수에 전달한다.
                markerset(kind_,distance_);

            }
        });



        mListView=(ListView)rootView.findViewById(mList);
        mListView.setOnItemClickListener(new ListViewItemClickListener());

        //mapView = (NMapView)rootView.findViewById(R.id.mapView2);


        //구글맵 변수 설정


        mapView=(MapView)rootView.findViewById(R.id.v_mapview2);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                //구글맵 변수값 얻어오기.
                googlemap=googleMap;


                googlemap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        //첫번째로 호출
                        //전체 정보 창에 사용할 뷰를 제공한다.
                        //선택한 뷰를 정보창의 뷰로 쓰겠다.
                        View v=getActivity().getLayoutInflater().inflate(R.layout.custom_marker,null);


                        //이디야커피#010-6284-8051#jw910911.vps.phps.kr/image/20.png
                        final String [] titletoken=marker.getTitle().split("#");

                        if(titletoken.length==1)
                        {
                            LinearLayout info = new LinearLayout(getActivity());
                            info.setOrientation(LinearLayout.VERTICAL);
                            info.setBackgroundResource(R.drawable.custommarkerbackground);
                            info.setPadding(17,5,17,5);

                            TextView title = new TextView(getActivity());
                            title.setTextColor(Color.BLACK);
                            title.setGravity(Gravity.CENTER);
                            title.setTypeface(null, Typeface.BOLD);
                            title.setText(marker.getTitle());

                            TextView snippet = new TextView(getActivity());
                            snippet.setTextColor(Color.GRAY);
                            snippet.setText(marker.getSnippet());

                            info.addView(title);
                            info.addView(snippet);

                            return info;
                        }

                        final TextView title=(TextView)v.findViewById(R.id.title);
                        final TextView phonenumber=(TextView)v.findViewById(R.id.number);
                        final ImageView image=(ImageView)v.findViewById(R.id.image);





                        title.setText(titletoken[0]);
                        phonenumber.setText(titletoken[1]);


                        final Marker marker_=marker;
                        //Glide.with(getActivity()).load(imageurl).override(250,250).transform(new CircleTransform(getActivity())).thumbnail(0.6f).into(circle);

                        Glide.with(getActivity()).load(titletoken[2]).thumbnail(0.3f).listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                                if(!isFromMemoryCache) marker_.showInfoWindow();
                                return false;
                            }
                        }).into(image);

                        detailNo=titletoken[3];









                        //뷰를 로드하는 시점에 이미지가 다운받지를 못함.

                        return v;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {

                        return null;
                    }
                });



                LatLng a=new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
                googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(a,16));

                //구글맵 마커 클릭 이벤트 등록

                googlemap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {

                        if(marker.getTitle().equals("지점"))
                        {
                            return ;
                        }


                        LatLng latlng=marker.getPosition();

                        final String spLatitude=mLocation.getLatitude()+"";
                        final String spLongitude=mLocation.getLongitude()+"";

                        final String epLatitude=latlng.latitude+"";
                        final String epLongitude=latlng.longitude+"";


                        final DialogInterface.OnClickListener detail=new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Intent intent=new Intent(getActivity(),restaurantViewActivity.class);
                                intent.putExtra("no",detailNo);
                                startActivity(intent);
                            }
                        };

                        final DialogInterface.OnClickListener getDirectionOnDaummap=new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Intent intent=new Intent(Intent.ACTION_VIEW);
                                String searchUrl="daummaps://route?sp="+spLatitude+","+spLongitude+"&ep="+epLatitude+","+epLongitude;
                                intent.setData(Uri.parse(searchUrl));
                                DaumMapSchemeURL daummap=new DaumMapSchemeURL(getActivity(),intent) {
                                    @Override
                                    public boolean canOpenDaummapURL() {
                                        return super.canOpenDaummapURL();
                                    }
                                };
                                if(daummap.existDaummapApp()){
                                    startActivity(intent);
                                } else {
                                    DaumMapSchemeURL.openDaummapDownloadPage(getActivity());
                                }
                            }
                        };

                        final DialogInterface.OnClickListener getDirectionOnfoot=new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //마커에 표시된 위치를 넘겨준다.
                                String [] latlng=new String [2];
                                latlng[0]=epLatitude;
                                latlng[1]=epLongitude;

                                new getDirectionOnFootThread().execute(latlng);
                            }
                        };


                        new AlertDialog.Builder(getActivity())
                                .setTitle("어떤 작업을 수행하시겠습니까?")
                                .setPositiveButton("지점" +
                                        "상세 정보",detail)
                                .setNeutralButton("도보로 길찾기",getDirectionOnfoot)
                                .setNegativeButton("다음지도 길찾기",getDirectionOnDaummap)
                                .show();


                    }
                });

            }
        });

        locationButton=(ImageButton) rootView.findViewById(R.id.locationButton2);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (getActivity() != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            myhandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    loading=ProgressDialog.show(getActivity(),"Loading...",
                                            "Please Wait...",false,false);

                                    //2번째 버튼 플래그를 켠다.
                                    LocationButton2Flag=true;

                                    //String [] a={"NETWORK","NETWORK","NETWORK"};

                                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                                    }
                                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

                                }
                            });
                        }
                    }).start();
                }
            }
        });


        //도보 안내 리스트뷰 초기화
        road_listview=(ListView)rootView.findViewById(R.id.road_listview);
        road_adapter=new Road_ListViewAdapter(getActivity());
        road_listview.setAdapter(road_adapter);


        return rootView;
    }

    public class getDirectionOnFootThread extends AsyncTask<String,Void,Void>{
        String mjson=null;
        ArrayList <HashMap<String,String>> point_hash=new ArrayList<>();
        ArrayList <HashMap<String,String[]>> line_hash=new ArrayList<>();

        @Override
        protected Void doInBackground(String... latlng) {

            BufferedReader bufferedReader=null;

            //LocationManager에서 입력받은 출발지 좌표

            String startX=mLocation.getLongitude()+"";//경도
            String startY=mLocation.getLatitude()+"";//위도

            //도보로 길찾기 클릭 리스너 에서 입력받은 도착지 좌표
            String endX=latlng[1];//경도
            String endY=latlng[0];//위도


            //출발지 UTF-8 인코딩

            String startName="%EC%B6%9C%EB%B0%9C%EC%A7%80";
            String endName="%EB%8F%84%EC%B0%A9%EC%A7%80";

            try{
                String stringurl="https://apis.skplanetx.com/tmap/routes/pedestrian?version=1&appKey=042a1189-21fc-36d0-8a0f-4c587be0ebd3&startX="+startX+"&startY="+startY+"&endX="+endX+"&endY="+endY+"&startName="+startName+"&endName="+endName+"&Content-Type=application/x-www-form-urlencoded&reqCoordType=WGS84GEO&resCoordType=WGS84GEO";
                                // "https://apis.skplanetx.com/tmap/routes/pedestrian?version=1&appKey=042a1189-21fc-36d0-8a0f-4c587be0ebd3&startX=126.9823439963945&startY=37.56461982743129&endX=126.98031634883303&endY=37.57007473965354&reqCoordType=WGS84GEO&startName=%EC%B6%9C%EB%B0%9C&endName=%EB%B3%B8%EC%82%AC&Content-Type=application/x-www-form-urlencoded"
                URL url = new URL(stringurl);
                HttpsURLConnection con=(HttpsURLConnection)url.openConnection();

                StringBuilder sb=new StringBuilder();
                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;

                while((line = bufferedReader.readLine())!= null){
                   sb.append(line+"\n");
                }

                //제이슨 값을 받아왔음
                mjson=sb.toString().trim();



                JSONObject jsonObject=new JSONObject(mjson);
                //features 컬럼으로 jsonArray생성
                JSONArray jsonArray=new JSONArray(jsonObject.getString("features"));

                for(int i=0;i<jsonArray.length();i++)
                {


                    //features하나의 값을 꺼낸다.
                    JSONObject jsonObject1=jsonArray.getJSONObject(i);

                    //geometry json값을 꺼낸다.
                    JSONObject geometry=jsonObject1.getJSONObject("geometry");


                    String type=geometry.getString("type");
                    if(type.equals("Point")) {
                        HashMap<String,String> hash=new HashMap<>();
                        String location = geometry.getString("coordinates");//좌표 형식 [126.123123123,32.123123123]
                        String longitude = location.substring(location.indexOf("[") + 1, location.indexOf(","));//경도 longitude
                        String latitude = location.substring(location.indexOf(",") + 1, location.indexOf("]"));
                        hash.put("longitude", longitude);
                        hash.put("latitude", latitude);

                        //properties json값을 꺼낸다.
                        JSONObject properties = jsonObject1.getJSONObject("properties");
                        String description = properties.getString("description");
                        hash.put("description", description);
                        point_hash.add(hash);
                    }
                    else if(type.equals("LineString"))
                    {
                        HashMap<String,String[]> hash=new HashMap<>();
                        String location = geometry.getString("coordinates");//좌표 형식 [126.123123123,32.123123123]

                        //파싱 개수와 각 좌표를 line hash에 집어 넣어야 한다.

                        //첫번째 대괄호와 마지막 대괄호를 자른다.
                        String sub_location=location.substring(1,location.length()-1);
                        String []parse_location=sub_location.split("],");



                        for(int j=0;j<parse_location.length;j++)
                        {
                            //],로 파싱한 문자열에서 [를 빼고 => 127.12313123,37.123123123123이런식으로 바꾼다.
                            parse_location[j]=parse_location[j].replace("[","");
                            parse_location[j]=parse_location[j].replace("]","");


                            String longitude = parse_location[j].substring(0,parse_location[j].indexOf(","));//경도 longitude
                            String latitude = parse_location[j].substring(parse_location[j].indexOf(",")+1,parse_location[j].length());

                            Log.e("경도값",longitude);
                            Log.e("위도값",latitude);

                            // longitude(경도,위도) 세트로 배열을 만든다.
                            //첫번째값 경도, 두번째값 위도
                            String [] latlng2=new String[]{longitude,latitude};
                            hash.put(j+"",latlng2);



                        }


                        /*
                        for(int j=0;j<parse_location.length;j++)
                        {
                            String longitude = parse_location[j].substring(location.indexOf("[") + 1, parse_location[j].indexOf(","));//경도 longitude
                            String latitude = parse_location[j].substring(location.indexOf(",") + 1, parse_location[j].indexOf("]"));
                            // longitude(경도,위도) 세트로 배열을 만든다.
                            //첫번째값 경도, 두번째값 위도
                            String [] latlng2=new String[]{longitude,latitude};
                            hash.put(j+"",latlng2);
                        }
                        */

                        line_hash.add(hash);



                        //String longitude = location.substring(location.indexOf("[") + 1, location.indexOf(","));//경도 longitude
                        //String latitude = location.substring(location.indexOf(",") + 1, location.indexOf("]"));
                        //hash.put("longitude", longitude);
                        //hash.put("latitude", latitude);




                    }

                }



            }catch(Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Toast.makeText(getActivity(),mjson,Toast.LENGTH_LONG).show();
            googlemap.clear();
            road_adapter.clearAdapter();


            //HashMap에 저장된 자료(tmap에서 파싱한 좌표와 description)을 구글 지도에 보여준다.

            for(int i=0;i<point_hash.size();i++)
            {
                String latitude=point_hash.get(i).get("latitude");
                String longitude=point_hash.get(i).get("longitude");

                LatLng latlng=new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latlng);


                //title이지만 음식점의 이름을 title을 보여줘야 함으로.. name을 입력
                String title="지점";
                markerOptions.title(title);

                //마커에 좌표에 대한 위치 설명(Description) 추가
                String description=point_hash.get(i).get("description");

                //이부분은 googlemap snipper떄문에 띄어쓰기 한 부분을 어뎁터에 는 적용하지 않으려고 변수 설정
                String description_=description;
                if(description.length()>=29) {
                    StringBuffer sb = new StringBuffer(description);
                    sb.insert(29,"\n");
                    description=sb.toString();
                }
                markerOptions.snippet(description);
                googlemap.addMarker(markerOptions);

                //adapter에 값 입력
                road_adapter.addItem(i+"",description_);
                //road_listview.setAdapter(road_adapter);
                road_adapter.notifyDataSetChanged();

            }

            //경위도 배열을 가지고 있는 각각의 hashmap 객체를 가져온다.
            for(int i=0;i<line_hash.size();i++)
            {
                PolylineOptions polyline_point = new PolylineOptions();

                //각각의 hashmap객체에서 String 배열을 추출하고 경도와 위도값을 가져온다.
                for(int j=0;j<line_hash.get(i).size();j++)
                {
                    //line_hash에서 key를 0,1,2,3,.. 으로 설정함.
                    //longitude(경도) latitude(위도) 를 배열에서 가져옴
                    String [] latlng_array=line_hash.get(i).get(j+"");
                    String longitude=latlng_array[0];
                    String latitude=latlng_array[1];
                    LatLng latlng=new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude));
                    polyline_point.add(latlng);
                }
                googlemap.addPolyline(polyline_point);
            }

            MarkerOptions markerOption=new MarkerOptions();
            markerOption.position(new LatLng(mLocation.getLatitude(),mLocation.getLongitude()));
            markerOption.title("현위치");
            markerOption.snippet("현재 위치입니다.");
            BitmapDescriptor icon= BitmapDescriptorFactory.fromResource(R.drawable.location3);
            markerOption.icon(icon);
            googlemap.addMarker(markerOption);

        }
    }

    public void markerset(String kind,String distance){



        ArrayList <LocationData> location_datas=new ArrayList<LocationData>();

        //location_datas리스트에 분류에 맞는 값을 추가하는 부분.
        for (LocationData item : locationdata) {

            if(kind.equals("all")&&distance.equals("0"))
            {
                location_datas.add(item);
                //itemList.add(item);

            }
            else if(kind.equals("all")&&item.distance<=Double.parseDouble(distance)*1000)
            {
                location_datas.add(item);
                //itemList.add(item);Toast.makeText(getActivity(),"LocationData의 거리"+(Double)item.distance+"선택한 거리"+Double.parseDouble(distance)*1000,Toast.LENGTH_LONG).show();

            }
            else if(item.kind.substring(0,3).equals(kind)&&distance.equals("0"))
            {
                location_datas.add(item);
                //itemList.add(item);

            }
            else if(item.kind.substring(0,3).equals(kind)&&item.distance<=Double.parseDouble(distance)*1000)
            {
                location_datas.add(item);
                //itemList.add(item);

            }
        }



        //지도의 오버레이 객체나 마커를 모두 지운다.
        googlemap.clear();

        //검색된 맛집이 없을때 예외처리

        if(location_datas.size()==0)
        {
            String kind_=null;

            if(kind.equals("mea")) {
                kind_="음식점이";
            }else if(kind.equals("des"))
            {
                kind_="디저트 맛집이";

            }else if(kind.equals("caf"))
            {
                kind_="카페가";
            }

            Toast.makeText(getActivity(),"지정된 거리내에 "+kind_+" 존재하지 않습니다.", Toast.LENGTH_LONG).show();
        }



        for(int i=0;i<location_datas.size();i++)
        {
            LatLng latlng=new LatLng(location_datas.get(i).getLatitude(),location_datas.get(i).getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latlng);


            //title이지만 음식점의 이름을 title을 보여줘야 함으로.. name을 입력
            String title=location_datas.get(i).getRes_name();
            String phonenumber=location_datas.get(i).getTelnumber();
            String imageurl=location_datas.get(i).getImageurl();
            String no=location_datas.get(i).getNo()+"";

            String titletoken=title+"#"+phonenumber+"#"+imageurl+"#"+no;
            markerOptions.title(titletoken);


            //번호가 null이 아니라면 snippet에 추가
            if(!location_datas.get(i).getTelnumber().equals(""))
                markerOptions.snippet(location_datas.get(i).getTelnumber());
            googlemap.addMarker(markerOptions);




        }


        //반원 추가
        LatLng latlng=new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
        CircleOptions circle=new CircleOptions().center(latlng)
                .radius(Integer.parseInt(distance)*1000)
                .strokeWidth(4f)
                .strokeColor(Color.parseColor("#FF0000"));
        googlemap.addCircle(circle);

        //자신의 위치를 나타내는 마커 설정

        MarkerOptions markerOption=new MarkerOptions();
        markerOption.position(latlng);
        markerOption.title("현위치");
        markerOption.snippet("현재 위치입니다.");
        BitmapDescriptor icon= BitmapDescriptorFactory.fromResource(R.drawable.location3);
        markerOption.icon(icon);
        googlemap.addMarker(markerOption);











    }

    public class ListViewItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            Object object=adapterView.getAdapter().getItem(i);
            ListData listdata=(ListData)object;


            Intent intent=new Intent(getActivity(),restaurantViewActivity.class);
            intent.putExtra("no",listdata.no+"");
            startActivity(intent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //mMapContext.onPause();
        //구글맵 onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //구글맵 onDestroy();
        mapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
        //mMapContext.onResume();

        //구글맵 onResume()
        mapView.onResume();



        if(tabIndex=="tag1")
        {


            myhandler=new Handler();
            Location location=null;

            //최근에 가져온 위치주소로 mLocation을 세팅한다
            if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!=null)
            {
                location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            }
            mLocation=location;


            //이부분은 녹스용 데이터
            /*
            mLocation=new Location("");
            //37.56, 126.97
            mLocation.setLongitude(126.97);
            mLocation.setLatitude(37.56);
            */
            //녹스용 데이터 끝

            //Toast.makeText(getActivity(),"onResume() 호출"+mLocation.getLatitude()+"",Toast.LENGTH_LONG).show();




            new GetDataJSON().execute("http://jw910911.vps.phps.kr/getdata.php");

            //지도에서 위치정보를 갱신한다.
            new getlocationdata().execute();


            //Log.e("location","latitude:"+location.getLatitude()+"longitude:"+location.getLongitude());


            //실행되지 않는 쓰레드 ▼
            if(location==null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        myhandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //String [] a={"NETWORK","NETWORK","NETWORK"};

                                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                                }
                                //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);

                            }
                        });
                    }
                }).start();

            }
        }else if (tabIndex=="tag2")
        {

            //오버레이 아이템
            int markerId=NMapPOIflagType.PIN;

            //NMapPOIdata poiData = new NMapPOIdata(2, mMapViewResourceProvider);
            //poiData.beginPOIdata(2);


            //마커를 입력시키는 함수
            markerset("all","0");

            //poiData.endPOIdata();

            // create POI data overlay
            //poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);

            // show all POI data
            //poiDataOverlay.showAllPOIdata(0);

            // set event listener to the overlay
            //poiDataOverlay.setOnStateChangeListener(this);

            //if(mLocation!=null)
            //mMapController.setMapCenter(new NGeoPoint(mLocation.getLongitude(),mLocation.getLatitude()), 18);



        }else if(tabIndex=="tag3")
        {
            Toast.makeText(getActivity(),"세번째탭",Toast.LENGTH_LONG).show();
        }

    }

    public class LocationData
    {
        double longitude;
        double latitude;
        String res_title;
        String res_name;
        String telnumber;
        String kind;
        String imageurl;
        Double distance;

        public String getImageurl() {
            return imageurl;
        }

        public void setImageurl(String imageurl) {
            this.imageurl = imageurl;
        }

        int no;

        public Double getDistance() {
            return distance;
        }

        public void setDistance(Double distance) {
            this.distance = distance;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getTelnumber() {
            return telnumber;
        }

        public void setTelnumber(String telnumber) {
            this.telnumber = telnumber;
        }


        public int getNo() {
            return no;
        }

        public void setNo(int no) {
            this.no = no;
        }

        public String getRes_name() {
            return res_name;
        }

        public void setRes_name(String res_name) {
            this.res_name = res_name;
        }

        public String getRes_title() {
            return res_title;
        }

        public void setRes_title(String res_title) {
            this.res_title = res_title;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

    public class getlocationdata extends AsyncTask<Void,String,Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {

            BufferedReader bufferedReader = null;
            try {
                URL url = new URL("http://jw910911.vps.phps.kr/mapget.php");
                HttpURLConnection con=(HttpURLConnection)url.openConnection();

                StringBuilder sb=new StringBuilder();
                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String json;
                while((json = bufferedReader.readLine())!= null){
                    sb.append(json+"\n");
                }

                json=sb.toString().trim();


                JSONObject jsonobj=new JSONObject(json);
                peoples=jsonobj.getJSONArray(TAG_RESULT);
                String longitude=null;
                String latitude=null;
                String res_title;
                String res_name;
                String no;
                String telnumber;
                String kind;
                String imageurl;


                for(int i=0;i<peoples.length();i++) {
                    JSONObject c = peoples.getJSONObject(i);
                    longitude = c.getString("longitude");
                    latitude = c.getString("latitude");
                    res_title=c.getString("title");
                    res_name=c.getString("name");
                    no=c.getString("no");
                    telnumber=c.getString("telnumber");
                    kind=c.getString("kind");
                    imageurl=c.getString("imageurl");


                    LocationData location=new LocationData();
                    location.setLatitude(Double.parseDouble(latitude));
                    location.setLongitude(Double.parseDouble(longitude));
                    location.setRes_title(res_title);
                    location.setRes_name(res_name);
                    location.setNo(Integer.parseInt(no));
                    location.setTelnumber(telnumber);
                    location.setKind(kind);
                    location.setImageurl(imageurl);

                    Location locationvalue=new Location("newLocation");
                    locationvalue.setLongitude(Double.parseDouble(longitude));
                    locationvalue.setLatitude(Double.parseDouble(latitude));

                    double distance_=0;




                    //mLocation은 최근에 가져온 위치정보
                    //mLoaction과의 위치를 arraylist에 추가시킨다.
                    if(mLocation!=null)
                    {
                        //거리를 float값으로 반환한다.
                        distance_=mLocation.distanceTo(locationvalue);

                    }
                    else
                    {
                        distance_=0;
                    }
                    location.setDistance(distance_);

                    locationdata.add(i,location);
                }

                //제이슨값 전달
                return null;


            }catch(Exception e)
            {
                e.printStackTrace();
                return null;
            }

        }


    }

    @Override
    public void onTabChanged(String tabId) {
        //String strMsg;
        //strMsg = "onTabChanged : " + tabId;

        tabIndex=tabId;
        onResume();
    }



        class GetDataJSON extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                String uri = params[0];

                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n");
                    }

                    return sb.toString().trim();

                }catch(Exception e){
                    return null;
                }
            }
            @Override
            protected void onPostExecute(String result){
                myJSON=result;
                showList();
            }
        }


    protected void showList()
    {

        final Handler mhandler=new Handler();




        Thread showListThread=new Thread(new Runnable() {
            @Override
            public void run() {
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        /*
                        final ProgressDialog loading2=ProgressDialog.show(getActivity(),"Loading...(s)",
                                "Please Wait...",false,false);
                        */
                        //리스트뷰 설정
                        mAdapter=new ListViewAdapter(getActivity());



                        try{
                            Toast.makeText(getActivity(),myJSON.substring(0,60),Toast.LENGTH_LONG).show();
                            JSONObject jsonobj=new JSONObject(myJSON);

                            peoples=jsonobj.getJSONArray(TAG_RESULT);

                            String price,title,info,name,url,kind;
                            double longitude,latitude;
                            int no;
                            for(int i=0;i<peoples.length();i++)
                            {
                                jsonobj=peoples.getJSONObject(i);
                                price=jsonobj.getString(TAG_PRICE);
                                title=jsonobj.getString(TAG_TITLE);
                                info=jsonobj.getString(TAG_INFO);
                                name=jsonobj.getString(TAG_NAME);
                                url=jsonobj.getString(TAG_URL);
                                no=jsonobj.getInt(TAG_NO);
                                longitude=jsonobj.getDouble(TAG_LNG);
                                latitude=jsonobj.getDouble(TAG_LAT);
                                kind=jsonobj.getString("kind");



                                /*
                                URL murl=new URL(url);
                                Bitmap bm=new getBitmap().execute(murl).get();
                                */
                                Log.e("adapter ",i+"번째 아이템 입력");

                                Location location=new Location("newLocation");
                                location.setLongitude(longitude);
                                location.setLatitude(latitude);

                                double distance=0;

                                //mLocation은 최근에 가져온 위치정보
                                //mLoaction과의 위치를 arraylist에 추가시킨다.
                                if(mLocation!=null)
                                {
                                    distance=mLocation.distanceTo(location);
                                }
                                else
                                {
                                    distance=0;
                                }


                                mAdapter.addItem(url,title,price,name,info,no,distance,kind);


                            }
                            mAdapter.sort();
                            mListView.setAdapter(mAdapter);

                        }catch(Exception e){
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

        showListThread.start();



    }



    private class ViewHolder{
        public ImageView mIcon;
        public TextView mText;
        public TextView mDate;
        public TextView resname;
        public TextView resinfo;
        public TextView distance;
    }

    private class road_ViewHolder{
        public TextView number;
        public TextView description;
    }

    private class Road_ListViewAdapter extends BaseAdapter{
        private ArrayList <Road_ListData> road_arraylist=new ArrayList<>();
        Context mContext;

        public void clearAdapter(){
            road_arraylist.clear();
        }

        public Road_ListViewAdapter(Context mContext)
        {
            this.mContext=mContext;
        }

        //실제로 데이터를 집어 넣는 부분
        public void addItem(String number,String description)
        {
            Road_ListData item=null;
            item=new Road_ListData();
            item.number=number;
            item.description=description;

            road_arraylist.add(item);
        }


        @Override
        public int getCount() {
            return road_arraylist.size();
        }

        @Override
        public Object getItem(int i) {
            return road_arraylist.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            //뷰들을 가지고 있는 뷰 홀더를 선언한다.
            road_ViewHolder holder;

            if(convertView==null) {
                //현재 return할 뷰가 존재하지 않으면 view홀더를 초기화한다.
                holder=new road_ViewHolder();
                //인플레이터 선언
                LayoutInflater inflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView=inflater.inflate(R.layout.road_listview_item,null);

                //viewHolder의 view값들을 초기화 한다.
                holder.number=(TextView) convertView.findViewById(R.id.number);
                holder.description=(TextView)convertView.findViewById(R.id.description);


                convertView.setTag(holder);


            }else
            {
                //현재 뷰가 한번 return됬다면 viewholder을 통해 재활용한다.
                holder=(road_ViewHolder)convertView.getTag();
            }

            Road_ListData item=road_arraylist.get(position);

            holder.number.setText(item.number);
            holder.description.setText(item.description);

            return convertView;
        }
    }




    private class ListViewAdapter extends BaseAdapter implements Filterable{

        private Context mContext=null;
        private ArrayList <ListData> mListData=new ArrayList<ListData>();
        Filter listFilter ;
        private ArrayList<ListData> filteredItemList = mListData ;


        @Override
        public Filter getFilter() {

            if (listFilter == null)
            {
                listFilter = new ListFilter() ;
            }
            return listFilter ;

        }
        private class ListFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();


                if (constraint == null || constraint.length() == 0) {
                    results.values = mListData;
                    results.count = mListData.size();
                } else {
                    ArrayList<ListData> itemList = new ArrayList<ListData>();
                    String kind;
                    String distance;

                    kind=constraint.toString().substring(0,3);
                    distance=constraint.toString().substring(3,4);

                    for (ListData item : mListData) {

                        //모든 데이터를 받아옴
                        if(kind.equals("all")&&distance.equals("0"))
                        {
                            itemList.add(item);
                        }
                        //만약 모든 분류이고 거리가 해당 거리KM*1000보다 작은 경우
                        else if(kind.equals("all")&&item.distance<=Double.parseDouble(distance)*1000)
                        {
                            itemList.add(item);
                        }
                        //arraylist안에 있는 데이터의 종류 값이 일치하고 거리는 필터링 하지 않은경우
                        else if(item.kind.substring(0,3).equals(kind)&&distance.equals("0"))
                        {
                            itemList.add(item);
                        }
                        //선택한 분류이고 거리가 해당 거리KM*1000보다 작은 경우
                        else if(item.kind.substring(0,3).equals(kind)&&item.distance<=Double.parseDouble(distance)*1000)
                        {
                            itemList.add(item);
                        }

                    }

                    results.values = itemList;
                    results.count = itemList.size();
                }
                return results;
            }


            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                // update listview by filtered data list.
                filteredItemList = (ArrayList<ListData>) results.values ;

                // notify
                if (results.count > 0) {
                    notifyDataSetChanged() ;
                } else {
                    notifyDataSetInvalidated() ;
                }
            }
        }



        //실제로 데이터를 집어 넣는 부분
        public void addItem(String imageurl, String mTitle, String mDate,String resname,String resinfo,int no,double distance,String kind)
        {
            ListData addInfo=null;
            addInfo=new ListData();
            addInfo.imageurl=imageurl;
            addInfo.mTitle=mTitle;
            addInfo.mDate=mDate;
            addInfo.resname=resname;
            addInfo.resinfo=resinfo;
            addInfo.no=no;
            addInfo.distance=distance;
            addInfo.kind=kind;

            mListData.add(0,addInfo);
        }

        public void remove(int position)
        {
            mListData.remove(position);
            dataChange();
        }

        public void sort()
        {
            Collections.sort(mListData,ListData.distanceComparator);
            dataChange();

        }

        public void dataChange(){
            mAdapter.notifyDataSetChanged();
        }


        public ListViewAdapter(Context mContext){
            super();
            this.mContext=mContext;
        }

        @Override
        public int getCount() {
            return filteredItemList.size();
        }

        @Override
        public Object getItem(int i) {
            return filteredItemList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        //리스트뷰안에 실제로 값을 처리하는 부분
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder;



            if(convertView==null)
            {


                holder=new ViewHolder();

                LayoutInflater inflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView=inflater.inflate(R.layout.listview_item,null);

                holder.mIcon=(ImageView)convertView.findViewById(R.id.mImage);
                holder.mText=(TextView)convertView.findViewById(R.id.mText);
                holder.mDate=(TextView)convertView.findViewById(R.id.mDate);
                holder.resname=(TextView)convertView.findViewById(R.id.resname);
                holder.resinfo=(TextView)convertView.findViewById(R.id.resinfo);
                holder.distance=(TextView)convertView.findViewById(R.id.distance);


                convertView.setTag(holder);
            }else{
                holder=(ViewHolder)convertView.getTag();
            }

            ListData mData=filteredItemList.get(position);


            if(mData.imageurl!=null)
            {
                holder.mIcon.setVisibility(View.VISIBLE);
                Glide.with(getActivity()).load(mData.imageurl).thumbnail(0.3f).into(holder.mIcon);
            }else{
                holder.mIcon.setVisibility(View.GONE);
            }

            holder.mText.setText(mData.mTitle);
            holder.mDate.setText(mData.mDate);
            holder.resname.setText(mData.resname);
            holder.resinfo.setText(mData.resinfo);
            holder.distance.setText(mData.getDistance());

            return convertView;
        }
    }

    public class getBitmap extends AsyncTask<URL,Void,Bitmap>
    {
        Bitmap bm;
        @Override
        protected Bitmap doInBackground(URL... urls) {
            try {

                URL url=urls[0];
                URLConnection urlc=url.openConnection();
                BufferedInputStream bis=new BufferedInputStream(urlc.getInputStream());


                bm= BitmapFactory.decodeStream(bis);



                bis.close();
                return bm;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /*//클러스터링은 포기..
    //MyItem은 생성자로 특정 지점을 입력하고 getPosition으로 위치를 반환한다.
    //getPosition은 ClusterItem의 추상메소드 함수이다.
    public class MyItem implements ClusterItem {
        private final LatLng mPosition;

        public MyItem(double lat, double lng) {
            mPosition = new LatLng(lat, lng);
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }
    }
    //클러스터 매니져 초기화 함수.


    private void setUpCluster()
    {
        mClusterManager=new ClusterManager<MyItem>(getActivity(),googlemap);

        //클러스터가 동작하도록 googlemap에 이벤트 추가
        googlemap.setOnCameraIdleListener(mClusterManager);

    }

    */


}


