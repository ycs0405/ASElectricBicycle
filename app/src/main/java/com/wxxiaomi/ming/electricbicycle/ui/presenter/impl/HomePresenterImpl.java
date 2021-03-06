package com.wxxiaomi.ming.electricbicycle.ui.presenter.impl;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.baidu.location.Address;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.wxxiaomi.ming.electricbicycle.ConstantValue;
import com.wxxiaomi.ming.electricbicycle.db.bean.UserCommonInfo;
import com.wxxiaomi.ming.electricbicycle.db.bean.UserLocatInfo;
import com.wxxiaomi.ming.electricbicycle.car.DriveActivity;
import com.wxxiaomi.ming.electricbicycle.car.TouchBoundActivity;
import com.wxxiaomi.ming.electricbicycle.manager.AccountHelper;
import com.wxxiaomi.ming.electricbicycle.manager.ShowerProvider;
import com.wxxiaomi.ming.electricbicycle.im.EmConstant;
import com.wxxiaomi.ming.electricbicycle.manager.UserFunctionProvider;
import com.wxxiaomi.ming.electricbicycle.im.notice.NoticeBean;
import com.wxxiaomi.ming.electricbicycle.im.notice.NoticeManager;
import com.wxxiaomi.ming.electricbicycle.common.rx.ToastObserver;
import com.wxxiaomi.ming.electricbicycle.ui.activity.FootPrintShowActivity;
import com.wxxiaomi.ming.electricbicycle.ui.activity.LoginActivity;
import com.wxxiaomi.ming.electricbicycle.ui.activity.UserInfoActivity;
import com.wxxiaomi.ming.electricbicycle.ui.activity.SettingActivity;
import com.wxxiaomi.ming.electricbicycle.ui.presenter.base.BasePreImpl;
import com.wxxiaomi.ming.electricbicycle.ui.presenter.HomePresenter;
import com.wxxiaomi.ming.electricbicycle.manager.LocatProvider;
import com.wxxiaomi.ming.electricbicycle.web.TestWebActivity;
import com.wxxiaomi.ming.electricbicycle.ui.activity.ContactActivity;
import com.wxxiaomi.ming.electricbicycle.ui.activity.view.HomeView;

import com.wxxiaomi.ming.electricbicycle.ui.weight.custom.CircularImageView;
import com.wxxiaomi.ming.touch.BluetoothHelper;

import java.util.List;

/**
 * Created by 12262 on 2016/6/6.
 * 主界面
 */
public class HomePresenterImpl extends BasePreImpl<HomeView> implements HomePresenter<HomeView>, NoticeManager.NoticeNotify {

    private LocationClient mLocClient;
    /**
     * 定位的模式
     */
    private MyLocationConfiguration.LocationMode mCurrentMode;
    /**
     * 定位完成后的监听
     */
    public MyLocationListenner myListener;
    private BaiduMap mBaiduMap;
    boolean isFirstLoc = true; // 是否首次定位
    /**
     * 当前所点击的附近的人的信息
     */
    private UserCommonInfo currentNearPerson;
    private List<UserLocatInfo> userLocatList;

    @Override
    public void init() {
        initMap(mView.getMap());
        NoticeManager.bindNotify(this);
    }



    @Override
    public void initMap(BaiduMap mBaiduMap) {
        this.mBaiduMap = mBaiduMap;
        myListener = new MyLocationListenner();
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(mView.getContext());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 5000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    @Override
    public void initViewData() {

    }

    @Override
    public void onMakerClick(Marker marker) {
        int zIndex = marker.getZIndex();
        UserCommonInfo tempUser = userLocatList.get(zIndex).userCommonInfo;
//        Log.i("wang",tempUser.toString());
        boolean isSame = (currentNearPerson == tempUser);
        currentNearPerson = tempUser;
        mView.editNearViewState(mView.isNearViewVis(), isSame);
    }

    @Override
    public void adapterNerarView(CircularImageView imageView, TextView tv_name, TextView tv_description) {
        ShowerProvider.showHead(mView.getContext(), imageView, currentNearPerson.avatar);
        tv_name.setText(currentNearPerson.nickname);
        tv_description.setText("生活就像海洋,只有意志坚定的人才能到彼岸");
    }


    @Override
    public void goBtnOnClick() {
//        mView.runActivity(SearchActiity.class,null);
    }

    @Override
    public void contactBtnOnClick() {
        mView.runActivity(ContactActivity.class, null);
    }

    @Override
    public void headBtnOnClick() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ConstantValue.INTENT_ISMINE, true);
        mView.runActivity(UserInfoActivity.class, bundle);
    }

    @Override
    public void locatBtnOnClick() {
        mView.scrollToMyLocat();
    }

    @Override
    public void nearHeadBtnOnClick() {
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(ConstantValue.INTENT_USERINFO, currentNearPerson);
//        bundle.putBoolean(ConstantValue.INTENT_ISMINE, false);
//        mView.runActivity(UserInfoActivity.class, bundle);
        UserInfoActivity.show(mView.getContext(),currentNearPerson);
    }

    @Override
    public void topicBtnOnClick() {
        Intent intent = new Intent(mView.getContext(), TestWebActivity.class);
        intent.putExtra("url", ConstantValue.SERVER_URL + "/app/topicList_1.html");
        mView.getContext().startActivity(intent);
    }

    @Override
    public void onNewIntent(Intent intent) {
//        Log.i("wang", "onNewIntent");
//        //EmConstant.ACCOUNT_CONFLICT
        boolean booleanExtra = intent.getBooleanExtra(EmConstant.ACCOUNT_CONFLICT, false);
        if(booleanExtra){
            mView.buildAlertDialog("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mView.runActivity(LoginActivity.class, null, true);
                }
            }, null, null, "提示", "您的账号在别的设备中登陆");
            mView.showDialog();
        }
    }

    @Override
    public void onSettingClick() {
        mView.runActivity(SettingActivity.class, null);
    }

    @Override
    public void onFootPrintClick() {
        mView.runActivity(FootPrintShowActivity.class, null);
//        mView.showSnackBar("足迹功能暂未开放");
    }

    @Override
    public void onAlbumClick() {
        mView.showSnackBar("相册功能暂未开放");
    }

    @Override
    public void onCollectClick() {
        mView.showSnackBar("收藏功能暂未开放");
    }

    @Override
    public void onFootPrintActionClick() {
        BluetoothHelper.getInstance().init(mView.getContext().getApplicationContext());
        if(BluetoothHelper.isEverDevice()){
//            intent = new Intent(HomeActivity.this,RunActivity2.class);
            mView.runActivity(DriveActivity.class, null);
        }else{
//            intent = new Intent(HomeActivity.this,BoundActivity.class);
            mView.runActivity(TouchBoundActivity.class, null);
        }

//        mView.runActivity(FootPublishActivity.class, null);
    }

    public void updateUnreadLabel() {
//        int allUnreadCount = ImHelper.getInstance().getAllUnreadCount();
//        mView.updateUnreadLabel(allUnreadCount);
//        ImHelper.getInstance().getAllUnreadCountRx()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<Integer>() {
//                    @Override
//                    public void call(Integer integer) {
//                        mView.updateUnreadLabel(integer);
//                    }
//                });
        NoticeBean bean = NoticeManager.getNotice();
        mView.updateUnreadLabel(bean.getInvite() + bean.getMessage() + bean.getReview());
    }


    @Override
    public void onViewDestory() {

        try {
            NoticeManager.unBindNotify(this);
            mLocClient.unRegisterLocationListener(myListener);
            mLocClient.stop();
            mBaiduMap.setMyLocationEnabled(false);
        }catch (Exception e){
            e.printStackTrace();
        }
//
    }

    @Override
    public void onViewResume() {
        ShowerProvider.showHead(mView.getContext(), mView.getHeadView(), AccountHelper.getAccountInfo().avatar);
        ShowerProvider.showHead(mView.getContext(), mView.getDrawerAvatar(), AccountHelper.getAccountInfo().avatar);
    }

    @Override
    public void onNoticeArrived(NoticeBean bean) {
        mView.updateUnreadLabel(bean.getInvite() + bean.getMessage() + bean.getReview());
    }


    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            int locType = location.getLocType();
//            Log.i("wang","locType:"+locType);

            // map view 销毁后不在处理新接收的位置
            if (location == null || mBaiduMap == null) {
                return;
            }
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
//            Log.i("wang", "获取到自己的位置latitude："+latitude+",longitude:"+longitude);
            Address address = location.getAddress();
            String locat_tag = location.getLocationDescribe();
            LocatProvider.getInstance().init(latitude, longitude, address, locat_tag);
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(0)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                mView.scrollToMyLocat();
                getNearByFromServer(latitude, longitude);
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    public void getNearByFromServer(final double latitude,
                                    final double longitude) {
        UserFunctionProvider.getInstance().getNearPeople(AccountHelper.getAccountInfo().id, latitude, longitude)
                .subscribe(
                        new ToastObserver<List<UserLocatInfo>>() {
                            @Override
                            public void onNext(List<UserLocatInfo> userLocatInfos) {
                                if (userLocatInfos != null) {
                                    userLocatList = userLocatInfos;
                                    for (int i = 0; i < userLocatInfos.size(); i++) {
                                        UserLocatInfo user = userLocatInfos.get(i);
                                        LatLng point = new LatLng(user.point[0], user.point[1]);
                                        mView.addMaker(point, i);
                                    }
                                }
                            }
                        }
                );

    }

}
