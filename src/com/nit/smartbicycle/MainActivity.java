package com.nit.smartbicycle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MKMapTouchListener;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class MainActivity extends Activity {
	private BMapManager mBMapMan = null;
	private MapView mMapView = null;// 地图View

	private boolean isRequest = false;// 是否手动触发请求定位
	private boolean isFirstLoc = true;// 是否首次定位
	// 定位相关
	LocationClient mLocClient;
	LocationData mLocData = null;
	// 定位图层
	LocationOverlay myLocationOverlay = null;
	// 弹出窗口图层
	//private PopupOverlay mPopupOverlay = null;
	private PopupOverlay pop = null;// 弹出泡泡图层，浏览节点时使用
	// 弹出窗口图层的View
	private View viewCache;
	private BDLocation location;
	// 用MapController完成地图控制
	private MapController mMapController = null;
	private ZoomControlView mZoomControlView;
	private ScaleView mScaleView;

	// 浏览路线节点相关
	MKRoute route = null;// 保存步行路线数据的变量，供浏览节点时使用
	RouteOverlay routeOverlay = null;
	Button mBtnPre = null;// 上一个节点
	Button mBtnNext = null;// 下一个节点
	int nodeIndex = -2;// 节点索引,供浏览节点时使用
	private TextView popupText = null;// 泡泡view
	//private View viewCacheR = null;
	// 搜索相关
	MKSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用

	// 按钮View
	// 道路状况
	private View vRoadcondition = null;
	private View vRoadc = null;
	private boolean roadConditionState = false;
	// 地图模式
	private View vMaplayers = null;
	private boolean twoD = true;
	// 附近
	private View vNearby = null;
	private View vNearbyIcon = null;
	private boolean nearOn = false;
	// 路线
	private View vRoute = null;
	// 定位
	private View vLocation = null;
	// 菜单
	private View vMenu = null;
	private View vMenuBtn = null;
	private boolean menuShow = false;
	private View vTimer = null;// 计时器
	private View vGuide = null;// 流程介绍
	private View vAbout = null;// 关于
	// 搜索面板
	private View vSearchPanel = null;
	private boolean bSHowSPanel = false;
	private View vChange = null;
	private EditText etStart = null;
	private EditText etEnd = null;
	private View vSearchBtn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 使用地图sdk前需先初始化BMapManager，这个必须在setContentView()先初始化
		mBMapMan = new BMapManager(getApplication());
		// 第一个参数是API key,
		// 第二个参数是常用事件监听，用来处理通常的网络错误，授权验证错误等，你也可以不添加这个回调接口
		mBMapMan.init("0oai2suZT4tvdruWdwT3zKBe", null);
		setContentView(R.layout.activity_main);

		// 点击按钮手动请求定位
		vLocation = (View) findViewById(R.id.location);
		vLocation.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				isRequest = true;
				mLocClient.requestLocation();
				Toast.makeText(MainActivity.this, "正在定位……", Toast.LENGTH_SHORT)
						.show();
			}
		});

		initMap();
		initLocation();

		// 道路状况
		vRoadcondition = (View) findViewById(R.id.roadcondition);
		vRoadc = (View) findViewById(R.id.icon_roadc);
		vRoadcondition.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!roadConditionState) {
					mMapView.setTraffic(true);
					roadConditionState = true;
					vRoadc.setBackgroundResource(R.drawable.main_icon_roadcondition_on);
					Toast.makeText(MainActivity.this, "显示交通情况",
							Toast.LENGTH_LONG).show();
				} else {
					mMapView.setTraffic(false);
					roadConditionState = false;
					vRoadc.setBackgroundResource(R.drawable.main_icon_roadcondition_off);
					Toast.makeText(MainActivity.this, "关闭交通情况显示",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		// 地图模式切换
		vMaplayers = (View) findViewById(R.id.maplayers);
		vMaplayers.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMapView.setSatellite(twoD);
				if (twoD) {
					twoD = false;
					Toast.makeText(MainActivity.this, "切换至卫星图",
							Toast.LENGTH_LONG).show();
				} else {
					twoD = true;
					Toast.makeText(MainActivity.this, "切换至2D图",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		// 显示周围站点
		vNearby = (View) findViewById(R.id.nearby);
		vNearbyIcon = (View) findViewById(R.id.icon_nearby);
		vNearby.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!nearOn) {
					// 显示周围站点
					// ...
					nearOn = true;
					Toast.makeText(MainActivity.this, "显示周围站点",
							Toast.LENGTH_LONG).show();
					vNearbyIcon.setBackgroundResource(R.drawable.nearby_on);
				} else {
					// 关闭显示
					// ...
					nearOn = false;
					Toast.makeText(MainActivity.this, "关闭站点显示",
							Toast.LENGTH_LONG).show();
					vNearbyIcon.setBackgroundResource(R.drawable.nearby);
				}
			}
		});

		// 路线规划
		vSearchPanel = (View) findViewById(R.id.searchPanel);
		vSearchPanel.setVisibility(View.GONE);
		vSearchPanel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchShow();
			}
		});
		vRoute = (View) findViewById(R.id.route);
		vRoute.setOnClickListener(new RouteListener());

		vSearchBtn = (View) findViewById(R.id.searchBtn);
		vSearchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String st = etStart.getText().toString();
				String ed = etEnd.getText().toString();
				if (st.equals("") || ed.equals("")) {
					Toast.makeText(MainActivity.this, "请输入起点或终点",
							Toast.LENGTH_SHORT).show();
				} else if (!st.equals("") && !ed.equals("")) {
					SearchShow();// 隐藏搜索面板
					// ...//发起搜索
					SearchButtonProcess(v, st, ed);
				}
			}
		});
		// 交换起终点
		vChange = (View) findViewById(R.id.routechange);
		etStart = (EditText) findViewById(R.id.start);
		etEnd = (EditText) findViewById(R.id.end);
		vChange.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String tmp = etStart.getText().toString();
				etStart.setText(etEnd.getText().toString());
				etEnd.setText(tmp);
			}
		});

		// 搜路线
		mBtnPre = (Button)findViewById(R.id.pre);
        mBtnNext = (Button)findViewById(R.id.next);
        mBtnPre.setVisibility(View.GONE);
		mBtnNext.setVisibility(View.GONE);
		OnClickListener nodeClickListener = new OnClickListener(){
			public void onClick(View v) {
				//浏览路线节点
				nodeClick(v);
			}
        };
		mBtnPre.setOnClickListener(nodeClickListener);
        mBtnNext.setOnClickListener(nodeClickListener);
		
		//创建 弹出泡泡图层
        createPaopao();
       
        //地图点击事件处理
        mMapView.regMapTouchListner(new MKMapTouchListener(){
			@Override
			public void onMapClick(GeoPoint point) {
			  //在此处理地图点击事件 
			  //消隐pop
			  if ( pop != null ){
				  pop.hidePop();
			  }
			}
			@Override
			public void onMapDoubleClick(GeoPoint point) {
			}
			@Override
			public void onMapLongClick(GeoPoint point) {
			}
        });
        
		// 初始化搜索模块，注册事件监听
		mSearch = new MKSearch();
		mSearch.init(mBMapMan, new MKSearchListener() {

			public void onGetDrivingRouteResult(MKDrivingRouteResult res,
					int error) {
			}

			public void onGetTransitRouteResult(MKTransitRouteResult res,
					int error) {
			}

			public void onGetWalkingRouteResult(MKWalkingRouteResult res,
					int error) {
				// 起点或终点有歧义，需要选择具体的城市列表或地址列表
				if (error == MKEvent.ERROR_ROUTE_ADDR) {
					// 遍历所有地址
					// ArrayList<MKPoiInfo> stPois =
					// res.getAddrResult().mStartPoiList;
					// ArrayList<MKPoiInfo> enPois =
					// res.getAddrResult().mEndPoiList;
					// ArrayList<MKCityListInfo> stCities =
					// res.getAddrResult().mStartCityList;
					// ArrayList<MKCityListInfo> enCities =
					// res.getAddrResult().mEndCityList;
					return;
				}
				if (error != 0 || res == null) {
					Toast.makeText(MainActivity.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
					return;
				}

				routeOverlay = new RouteOverlay(MainActivity.this, mMapView);
				// 此处仅展示一个方案作为示例
				routeOverlay.setData(res.getPlan(0).getRoute(0));
				// 清除其他图层
				//mMapView.getOverlays().clear();
				// 添加路线图层
				mMapView.getOverlays().add(routeOverlay);
				// 执行刷新使生效
				mMapView.refresh();
				// 使用zoomToSpan()绽放地图，使路线能完全显示在地图上
				mMapView.getController().zoomToSpan(
						routeOverlay.getLatSpanE6(),
						routeOverlay.getLonSpanE6());
				// 移动地图到起点
				mMapView.getController().animateTo(res.getStart().pt);
				// 将路线数据保存给全局变量
				route = res.getPlan(0).getRoute(0);
				// 重置路线节点索引，节点浏览时使用
				nodeIndex = -1;
				mBtnPre.setVisibility(View.VISIBLE);
				mBtnNext.setVisibility(View.VISIBLE);

			}

			public void onGetAddrResult(MKAddrInfo res, int error) {
			}

			public void onGetPoiResult(MKPoiResult res, int arg1, int arg2) {
			}

			public void onGetBusDetailResult(MKBusLineResult result, int iError) {
			}

			@Override
			public void onGetSuggestionResult(MKSuggestionResult res, int arg1) {
			}

			@Override
			public void onGetPoiDetailSearchResult(int type, int iError) {
			}

			@Override
			public void onGetShareUrlResult(MKShareUrlResult result, int type,
					int error) {
			}
		});

		MenuListener ml = new MenuListener();
		// 菜单显示
		vMenuBtn = (View) findViewById(R.id.menuBtn);
		vMenuBtn.setOnClickListener(ml);
		vMenu = (View) findViewById(R.id.menulist);
		vMenu.setVisibility(View.GONE);
		vMenu.setOnClickListener(ml);

		ActivityListener al = new ActivityListener();
		// 计时器
		vTimer = (View) findViewById(R.id.timer);
		vTimer.setOnClickListener(al);
		// 流程介绍
		vGuide = (View) findViewById(R.id.guide);
		vGuide.setOnClickListener(al);
		// 关于
		vAbout = (View) findViewById(R.id.about);
		vAbout.setOnClickListener(al);
	}

	/**
	 * 发起路线规划搜索示例
	 * 
	 * @param v
	 */
	void SearchButtonProcess(View v, String st, String ed) {
		// 重置浏览节点的路线数据
		route = null;
		routeOverlay = null;
		mBtnPre.setVisibility(View.INVISIBLE);
		mBtnNext.setVisibility(View.INVISIBLE);
		// 处理搜索按钮响应
//		EditText editSt = (EditText) findViewById(R.id.start);
//		EditText editEn = (EditText) findViewById(R.id.end);

		// 对起点终点的name进行赋值，也可以直接对坐标赋值，赋值坐标则将根据坐标进行搜索
		MKPlanNode stNode = new MKPlanNode();
		stNode.name = st;
		MKPlanNode enNode = new MKPlanNode();
		enNode.name = ed;

		// 实际使用中请对起点终点城市进行正确的设定
		mSearch.walkingSearch("宁波", stNode, "宁波", enNode);
	}

	/**
	 * 节点浏览示例
	 * 
	 * @param v
	 */
	public void nodeClick(View v) {
		createPaopao();
		// 驾车、步行使用的数据结构相同，因此类型为驾车或步行，节点浏览方法相同
		if (nodeIndex < -1 || route == null || nodeIndex >= route.getNumSteps())
			return;

		// 上一个节点
		if (mBtnPre.equals(v) && nodeIndex > 0) {
			// 索引减
			nodeIndex--;
			// 移动到指定索引的坐标
			mMapView.getController().animateTo(
					route.getStep(nodeIndex).getPoint());
			// 弹出泡泡
			popupText.setBackgroundResource(R.drawable.popup);
			popupText.setText(route.getStep(nodeIndex).getContent());
			pop.showPopup(getBitmapFromView(popupText), route
					.getStep(nodeIndex).getPoint(), 5);
		}
		// 下一个节点
		if (mBtnNext.equals(v) && nodeIndex < (route.getNumSteps() - 1)) {
			// 索引加
			nodeIndex++;
			// 移动到指定索引的坐标
			mMapView.getController().animateTo(
					route.getStep(nodeIndex).getPoint());
			// 弹出泡泡
			popupText.setBackgroundResource(R.drawable.popup);
			popupText.setText(route.getStep(nodeIndex).getContent());
			pop.showPopup(getBitmapFromView(popupText), route
					.getStep(nodeIndex).getPoint(), 5);
		}

	}

	/**
	 * 创建弹出泡泡图层
	 */
	public void createPaopao() {
		viewCache = getLayoutInflater().inflate(R.layout.pop_layout, null);
        popupText = (TextView) viewCache.findViewById(R.id.textcache);
		// 泡泡点击响应回调
		PopupClickListener popListener = new PopupClickListener() {
			@Override
			public void onClickedPopup(int index) {
				Log.v("click", "clickapoapo");
			}
		};
		pop = new PopupOverlay(mMapView, popListener);
	}

	class RouteListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			SearchShow();// 开启搜索面板
		}
	}

	private void SearchShow() {
		if (bSHowSPanel) {
			vSearchPanel.setVisibility(View.GONE);
			bSHowSPanel = false;
		} else {
			vSearchPanel.setVisibility(View.VISIBLE);
			bSHowSPanel = true;
		}
	}

	class MenuListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (menuShow) {
				vMenu.setVisibility(View.GONE);
				menuShow = false;
			} else {
				vMenu.setVisibility(View.VISIBLE);
				menuShow = true;
			}
		}
	}

	class ActivityListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			switch (v.getId()) {
			case R.id.timer:
				intent.setClass(MainActivity.this, TimerActivity.class);
				startActivity(intent);
				break;
			case R.id.guide:
				intent.setClass(MainActivity.this, GuideActivity.class);
				startActivity(intent);
				break;
			case R.id.about:
				intent.setClass(MainActivity.this, AboutActivity.class);
				startActivity(intent);
				break;
			}
		}
	}

	private void initMap() {
		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapsView);
		// 隐藏自带的地图缩放控件
		mMapView.setBuiltInZoomControls(false);

		mScaleView = (ScaleView) findViewById(R.id.scaleView);
		mScaleView.setMapView(mMapView);
		mZoomControlView = (ZoomControlView) findViewById(R.id.ZoomControlView);
		mZoomControlView.setMapView(mMapView);

		// 地图显示事件监听器。 该接口监听地图显示事件，用户需要实现该接口以处理相应事件。
		mMapView.regMapViewListener(mBMapMan, new MKMapViewListener() {

			@Override
			public void onMapMoveFinish() {
				refreshScaleAndZoomControl();
			}

			@Override
			public void onMapLoadFinish() {

			}

			/**
			 * 动画结束时会回调此消息.我们在此方法里面更新缩放按钮的状态
			 */
			@Override
			public void onMapAnimationFinish() {
				refreshScaleAndZoomControl();
			}

			@Override
			public void onGetCurrentMap(Bitmap arg0) {

			}

			@Override
			public void onClickMapPoi(MapPoi arg0) {

			}
		});

		mMapController = mMapView.getController();
		mMapView.getController().setZoom(14);
		mMapView.getController().enableClick(true);
		// mMapController.setOverlooking(-45); //设置地图俯视角度 ，范围：0~ -45

		refreshScaleAndZoomControl();

//		viewCache = LayoutInflater.from(this)
//				.inflate(R.layout.pop_layout, null);
//		pop = new PopupOverlay(mMapView, new PopupClickListener() {
//			@Override
//			public void onClickedPopup(int arg0) {
//				pop.hidePop();
//			}
//		});
	}

	private void initLocation() {
		// 定位初始化
		mLocData = new LocationData();
		mLocClient = new LocationClient(getApplicationContext());
		mLocClient.registerLocationListener(new BDLocationListenerImpl());// 注册定位监听接口

		/**
		 * 设置定位参数
		 */
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 打开GPRS
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(5000); // 设置发起定位请求的间隔时间为5000ms
		option.disableCache(false);// 禁止启用缓存定位
		// option.setPoiNumber(5); //最多返回POI个数
		// option.setPoiDistance(1000); //poi查询距离
		// option.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息

		mLocClient.setLocOption(option);
		mLocClient.start(); // 调用此方法开始定位

		// 定位图层初始化
		myLocationOverlay = new LocationOverlay(mMapView);
		// 设置定位数据
		myLocationOverlay.setData(mLocData);

		// myLocationOverlay.setMarker(getResources().getDrawable(R.drawable.location_arrows));
		// 添加定位图层
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		// 修改定位数据后刷新图层生效
		mMapView.refresh();
	}

	/**
	 * 定位接口，需要实现两个方法
	 * 
	 * @author xiaanming
	 * 
	 */
	public class BDLocationListenerImpl implements BDLocationListener {

		/**
		 * 接收异步返回的定位结果，参数是BDLocation类型参数
		 */
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}

			MainActivity.this.location = location;

			mLocData.latitude = location.getLatitude();
			mLocData.longitude = location.getLongitude();
			// 如果不显示定位精度圈，将accuracy赋值为0即可
			mLocData.accuracy = location.getRadius();
			mLocData.direction = location.getDerect();

			// 将定位数据设置到定位图层里
			myLocationOverlay.setData(mLocData);
			// 更新图层数据执行刷新后生效
			mMapView.refresh();

			if (isFirstLoc || isRequest) {
				mMapController.animateTo(new GeoPoint((int) (location
						.getLatitude() * 1e6),
						(int) (location.getLongitude() * 1e6)));

				showPopupOverlay(location);

				isRequest = false;
			}

			isFirstLoc = false;
		}

		/**
		 * 接收异步返回的POI查询结果，参数是BDLocation类型参数
		 */
		@Override
		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}

	}
  	private void showPopupOverlay(BDLocation location) {
        popupText = ((TextView) viewCache
                .findViewById(R.id.textcache));
        String loc = location.getAddrStr();
        if (loc != null)
            popupText.setText("[我的位置]\n" + loc);
        else
            popupText.setText("[我的位置]");
        pop.showPopup(getBitmapFromView(popupText),
                new GeoPoint((int) (location.getLatitude() * 1e6),
                        (int) (location.getLongitude() * 1e6)), 10);
    }
	// 继承MyLocationOverlay重写dispatchTap方法
	private class LocationOverlay extends MyLocationOverlay {

		public LocationOverlay(MapView arg0) {
			super(arg0);
		}

		/**
		 * 在“我的位置”坐标上处理点击事件。
		 */
		@Override
		protected boolean dispatchTap() {
			// 点击我的位置显示PopupOverlay
			showPopupOverlay(location);
			return super.dispatchTap();
		}

	}

	private void refreshScaleAndZoomControl() {
		// 更新缩放按钮的状态
		mZoomControlView.refreshZoomButtonStatus(Math.round(mMapView
				.getZoomLevel()));
		mScaleView.refreshScaleView(Math.round(mMapView.getZoomLevel()));
	}

	@Override
	protected void onDestroy() {
		// MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		mMapView.destroy();
		
		mSearch.destory();

		// 退出应用调用BMapManager的destroy()方法
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}

		// 退出时销毁定位
		if (mLocClient != null) {
			mLocClient.stop();
		}

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		mMapView.onPause();
		if (mBMapMan != null) {
			mBMapMan.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		mMapView.onResume();
		if (mBMapMan != null) {
			mBMapMan.start();
		}
		super.onResume();
	}

	/**
	 * 
	 * @param view
	 * @return
	 */
	public static Bitmap getBitmapFromView(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		return bitmap;
	}

}