package com.example.baidumap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity {
    LocationClient mLocationClient;  //定位客户端
    MapView mapView;  //Android Widget地图控件
    BaiduMap baiduMap;
    boolean isFirstLocate = true;

    TextView tv_Lat;  //纬度
    TextView tv_Lon;  //经度
    TextView tv_Add;  //地址

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果没有定位权限，动态请求用户允许使用该权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else {
            requestLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "没有定位权限！", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    requestLocation();
                }
        }
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
        mLocationClient.requestLocation();
    }

    private void initLocation() {  //初始化
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(new MyLocationListener());
        setContentView(R.layout.activity_main);
        tv_Lat = findViewById(R.id.tv_Lat);
        tv_Lon = findViewById(R.id.tv_Lon);
        tv_Add = findViewById(R.id.tv_Add);
        mapView = findViewById(R.id.bmapView);
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        LocationClientOption option = new LocationClientOption();
        //设置扫描时间间隔
        option.setScanSpan(1000);
        //设置定位模式
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //设置需要地址信息
        option.setIsNeedAddress(true);
        //设置坐标类型
        option.setCoorType("bd09ll");
        //打开GPS
        option.setOpenGps(true);
        //保存定位参数
        mLocationClient.setLocOption(option);
    }

    //内部类，百度位置监听器
    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            tv_Lat.setText(bdLocation.getLatitude()+"");
            tv_Lon.setText(bdLocation.getLongitude()+"");
            tv_Add.setText(bdLocation.getAddrStr());

            if (bdLocation == null || mapView == null){
                return;
            }

            if (isFirstLocate) {
                isFirstLocate = false;
                //设置并显示中心点
                setPosition2Center(baiduMap, bdLocation, true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
    }

    public void setPosition2Center(BaiduMap map, BDLocation bdLocation, Boolean isShowLoc) {
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(bdLocation.getRadius())
                .direction(bdLocation.getRadius()).latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude()).build();
        map.setMyLocationData(locData);
        if (isShowLoc) {
            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(18.0f);
            map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }
}
