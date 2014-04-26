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
	private MapView mMapView = null;// ��ͼView

	private boolean isRequest = false;// �Ƿ��ֶ���������λ
	private boolean isFirstLoc = true;// �Ƿ��״ζ�λ
	// ��λ���
	LocationClient mLocClient;
	LocationData mLocData = null;
	// ��λͼ��
	LocationOverlay myLocationOverlay = null;
	// ��������ͼ��
	//private PopupOverlay mPopupOverlay = null;
	private PopupOverlay pop = null;// ��������ͼ�㣬����ڵ�ʱʹ��
	// ��������ͼ���View
	private View viewCache;
	private BDLocation location;
	// ��MapController��ɵ�ͼ����
	private MapController mMapController = null;
	private ZoomControlView mZoomControlView;
	private ScaleView mScaleView;

	// ���·�߽ڵ����
	MKRoute route = null;// ���沽��·�����ݵı�����������ڵ�ʱʹ��
	RouteOverlay routeOverlay = null;
	Button mBtnPre = null;// ��һ���ڵ�
	Button mBtnNext = null;// ��һ���ڵ�
	int nodeIndex = -2;// �ڵ�����,������ڵ�ʱʹ��
	private TextView popupText = null;// ����view
	//private View viewCacheR = null;
	// �������
	MKSearch mSearch = null; // ����ģ�飬Ҳ��ȥ����ͼģ�����ʹ��

	// ��ťView
	// ��·״��
	private View vRoadcondition = null;
	private View vRoadc = null;
	private boolean roadConditionState = false;
	// ��ͼģʽ
	private View vMaplayers = null;
	private boolean twoD = true;
	// ����
	private View vNearby = null;
	private View vNearbyIcon = null;
	private boolean nearOn = false;
	// ·��
	private View vRoute = null;
	// ��λ
	private View vLocation = null;
	// �˵�
	private View vMenu = null;
	private View vMenuBtn = null;
	private boolean menuShow = false;
	private View vTimer = null;// ��ʱ��
	private View vGuide = null;// ���̽���
	private View vAbout = null;// ����
	// �������
	private View vSearchPanel = null;
	private boolean bSHowSPanel = false;
	private View vChange = null;
	private EditText etStart = null;
	private EditText etEnd = null;
	private View vSearchBtn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ʹ�õ�ͼsdkǰ���ȳ�ʼ��BMapManager�����������setContentView()�ȳ�ʼ��
		mBMapMan = new BMapManager(getApplication());
		// ��һ��������API key,
		// �ڶ��������ǳ����¼���������������ͨ�������������Ȩ��֤����ȣ���Ҳ���Բ��������ص��ӿ�
		mBMapMan.init("0oai2suZT4tvdruWdwT3zKBe", null);
		setContentView(R.layout.activity_main);

		// �����ť�ֶ�����λ
		vLocation = (View) findViewById(R.id.location);
		vLocation.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				isRequest = true;
				mLocClient.requestLocation();
				Toast.makeText(MainActivity.this, "���ڶ�λ����", Toast.LENGTH_SHORT)
						.show();
			}
		});

		initMap();
		initLocation();

		// ��·״��
		vRoadcondition = (View) findViewById(R.id.roadcondition);
		vRoadc = (View) findViewById(R.id.icon_roadc);
		vRoadcondition.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!roadConditionState) {
					mMapView.setTraffic(true);
					roadConditionState = true;
					vRoadc.setBackgroundResource(R.drawable.main_icon_roadcondition_on);
					Toast.makeText(MainActivity.this, "��ʾ��ͨ���",
							Toast.LENGTH_LONG).show();
				} else {
					mMapView.setTraffic(false);
					roadConditionState = false;
					vRoadc.setBackgroundResource(R.drawable.main_icon_roadcondition_off);
					Toast.makeText(MainActivity.this, "�رս�ͨ�����ʾ",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		// ��ͼģʽ�л�
		vMaplayers = (View) findViewById(R.id.maplayers);
		vMaplayers.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMapView.setSatellite(twoD);
				if (twoD) {
					twoD = false;
					Toast.makeText(MainActivity.this, "�л�������ͼ",
							Toast.LENGTH_LONG).show();
				} else {
					twoD = true;
					Toast.makeText(MainActivity.this, "�л���2Dͼ",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		// ��ʾ��Χվ��
		vNearby = (View) findViewById(R.id.nearby);
		vNearbyIcon = (View) findViewById(R.id.icon_nearby);
		vNearby.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!nearOn) {
					// ��ʾ��Χվ��
					// ...
					nearOn = true;
					Toast.makeText(MainActivity.this, "��ʾ��Χվ��",
							Toast.LENGTH_LONG).show();
					vNearbyIcon.setBackgroundResource(R.drawable.nearby_on);
				} else {
					// �ر���ʾ
					// ...
					nearOn = false;
					Toast.makeText(MainActivity.this, "�ر�վ����ʾ",
							Toast.LENGTH_LONG).show();
					vNearbyIcon.setBackgroundResource(R.drawable.nearby);
				}
			}
		});

		// ·�߹滮
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
					Toast.makeText(MainActivity.this, "�����������յ�",
							Toast.LENGTH_SHORT).show();
				} else if (!st.equals("") && !ed.equals("")) {
					SearchShow();// �����������
					// ...//��������
					SearchButtonProcess(v, st, ed);
				}
			}
		});
		// �������յ�
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

		// ��·��
		mBtnPre = (Button)findViewById(R.id.pre);
        mBtnNext = (Button)findViewById(R.id.next);
        mBtnPre.setVisibility(View.GONE);
		mBtnNext.setVisibility(View.GONE);
		OnClickListener nodeClickListener = new OnClickListener(){
			public void onClick(View v) {
				//���·�߽ڵ�
				nodeClick(v);
			}
        };
		mBtnPre.setOnClickListener(nodeClickListener);
        mBtnNext.setOnClickListener(nodeClickListener);
		
		//���� ��������ͼ��
        createPaopao();
       
        //��ͼ����¼�����
        mMapView.regMapTouchListner(new MKMapTouchListener(){
			@Override
			public void onMapClick(GeoPoint point) {
			  //�ڴ˴����ͼ����¼� 
			  //����pop
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
        
		// ��ʼ������ģ�飬ע���¼�����
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
				// �����յ������壬��Ҫѡ�����ĳ����б���ַ�б�
				if (error == MKEvent.ERROR_ROUTE_ADDR) {
					// �������е�ַ
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
					Toast.makeText(MainActivity.this, "��Ǹ��δ�ҵ����",
							Toast.LENGTH_SHORT).show();
					return;
				}

				routeOverlay = new RouteOverlay(MainActivity.this, mMapView);
				// �˴���չʾһ��������Ϊʾ��
				routeOverlay.setData(res.getPlan(0).getRoute(0));
				// �������ͼ��
				//mMapView.getOverlays().clear();
				// ���·��ͼ��
				mMapView.getOverlays().add(routeOverlay);
				// ִ��ˢ��ʹ��Ч
				mMapView.refresh();
				// ʹ��zoomToSpan()���ŵ�ͼ��ʹ·������ȫ��ʾ�ڵ�ͼ��
				mMapView.getController().zoomToSpan(
						routeOverlay.getLatSpanE6(),
						routeOverlay.getLonSpanE6());
				// �ƶ���ͼ�����
				mMapView.getController().animateTo(res.getStart().pt);
				// ��·�����ݱ����ȫ�ֱ���
				route = res.getPlan(0).getRoute(0);
				// ����·�߽ڵ��������ڵ����ʱʹ��
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
		// �˵���ʾ
		vMenuBtn = (View) findViewById(R.id.menuBtn);
		vMenuBtn.setOnClickListener(ml);
		vMenu = (View) findViewById(R.id.menulist);
		vMenu.setVisibility(View.GONE);
		vMenu.setOnClickListener(ml);

		ActivityListener al = new ActivityListener();
		// ��ʱ��
		vTimer = (View) findViewById(R.id.timer);
		vTimer.setOnClickListener(al);
		// ���̽���
		vGuide = (View) findViewById(R.id.guide);
		vGuide.setOnClickListener(al);
		// ����
		vAbout = (View) findViewById(R.id.about);
		vAbout.setOnClickListener(al);
	}

	/**
	 * ����·�߹滮����ʾ��
	 * 
	 * @param v
	 */
	void SearchButtonProcess(View v, String st, String ed) {
		// ��������ڵ��·������
		route = null;
		routeOverlay = null;
		mBtnPre.setVisibility(View.INVISIBLE);
		mBtnNext.setVisibility(View.INVISIBLE);
		// ����������ť��Ӧ
//		EditText editSt = (EditText) findViewById(R.id.start);
//		EditText editEn = (EditText) findViewById(R.id.end);

		// ������յ��name���и�ֵ��Ҳ����ֱ�Ӷ����긳ֵ����ֵ�����򽫸��������������
		MKPlanNode stNode = new MKPlanNode();
		stNode.name = st;
		MKPlanNode enNode = new MKPlanNode();
		enNode.name = ed;

		// ʵ��ʹ�����������յ���н�����ȷ���趨
		mSearch.walkingSearch("����", stNode, "����", enNode);
	}

	/**
	 * �ڵ����ʾ��
	 * 
	 * @param v
	 */
	public void nodeClick(View v) {
		createPaopao();
		// �ݳ�������ʹ�õ����ݽṹ��ͬ���������Ϊ�ݳ����У��ڵ����������ͬ
		if (nodeIndex < -1 || route == null || nodeIndex >= route.getNumSteps())
			return;

		// ��һ���ڵ�
		if (mBtnPre.equals(v) && nodeIndex > 0) {
			// ������
			nodeIndex--;
			// �ƶ���ָ������������
			mMapView.getController().animateTo(
					route.getStep(nodeIndex).getPoint());
			// ��������
			popupText.setBackgroundResource(R.drawable.popup);
			popupText.setText(route.getStep(nodeIndex).getContent());
			pop.showPopup(getBitmapFromView(popupText), route
					.getStep(nodeIndex).getPoint(), 5);
		}
		// ��һ���ڵ�
		if (mBtnNext.equals(v) && nodeIndex < (route.getNumSteps() - 1)) {
			// ������
			nodeIndex++;
			// �ƶ���ָ������������
			mMapView.getController().animateTo(
					route.getStep(nodeIndex).getPoint());
			// ��������
			popupText.setBackgroundResource(R.drawable.popup);
			popupText.setText(route.getStep(nodeIndex).getContent());
			pop.showPopup(getBitmapFromView(popupText), route
					.getStep(nodeIndex).getPoint(), 5);
		}

	}

	/**
	 * ������������ͼ��
	 */
	public void createPaopao() {
		viewCache = getLayoutInflater().inflate(R.layout.pop_layout, null);
        popupText = (TextView) viewCache.findViewById(R.id.textcache);
		// ���ݵ����Ӧ�ص�
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
			SearchShow();// �����������
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
		// ��ͼ��ʼ��
		mMapView = (MapView) findViewById(R.id.bmapsView);
		// �����Դ��ĵ�ͼ���ſؼ�
		mMapView.setBuiltInZoomControls(false);

		mScaleView = (ScaleView) findViewById(R.id.scaleView);
		mScaleView.setMapView(mMapView);
		mZoomControlView = (ZoomControlView) findViewById(R.id.ZoomControlView);
		mZoomControlView.setMapView(mMapView);

		// ��ͼ��ʾ�¼��������� �ýӿڼ�����ͼ��ʾ�¼����û���Ҫʵ�ָýӿ��Դ�����Ӧ�¼���
		mMapView.regMapViewListener(mBMapMan, new MKMapViewListener() {

			@Override
			public void onMapMoveFinish() {
				refreshScaleAndZoomControl();
			}

			@Override
			public void onMapLoadFinish() {

			}

			/**
			 * ��������ʱ��ص�����Ϣ.�����ڴ˷�������������Ű�ť��״̬
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
		// mMapController.setOverlooking(-45); //���õ�ͼ���ӽǶ� ����Χ��0~ -45

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
		// ��λ��ʼ��
		mLocData = new LocationData();
		mLocClient = new LocationClient(getApplicationContext());
		mLocClient.registerLocationListener(new BDLocationListenerImpl());// ע�ᶨλ�����ӿ�

		/**
		 * ���ö�λ����
		 */
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // ��GPRS
		option.setAddrType("all");// ���صĶ�λ���������ַ��Ϣ
		option.setCoorType("bd09ll");// ���صĶ�λ����ǰٶȾ�γ��,Ĭ��ֵgcj02
		option.setScanSpan(5000); // ���÷���λ����ļ��ʱ��Ϊ5000ms
		option.disableCache(false);// ��ֹ���û��涨λ
		// option.setPoiNumber(5); //��෵��POI����
		// option.setPoiDistance(1000); //poi��ѯ����
		// option.setPoiExtraInfo(true); //�Ƿ���ҪPOI�ĵ绰�͵�ַ����ϸ��Ϣ

		mLocClient.setLocOption(option);
		mLocClient.start(); // ���ô˷�����ʼ��λ

		// ��λͼ���ʼ��
		myLocationOverlay = new LocationOverlay(mMapView);
		// ���ö�λ����
		myLocationOverlay.setData(mLocData);

		// myLocationOverlay.setMarker(getResources().getDrawable(R.drawable.location_arrows));
		// ��Ӷ�λͼ��
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		// �޸Ķ�λ���ݺ�ˢ��ͼ����Ч
		mMapView.refresh();
	}

	/**
	 * ��λ�ӿڣ���Ҫʵ����������
	 * 
	 * @author xiaanming
	 * 
	 */
	public class BDLocationListenerImpl implements BDLocationListener {

		/**
		 * �����첽���صĶ�λ�����������BDLocation���Ͳ���
		 */
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}

			MainActivity.this.location = location;

			mLocData.latitude = location.getLatitude();
			mLocData.longitude = location.getLongitude();
			// �������ʾ��λ����Ȧ����accuracy��ֵΪ0����
			mLocData.accuracy = location.getRadius();
			mLocData.direction = location.getDerect();

			// ����λ�������õ���λͼ����
			myLocationOverlay.setData(mLocData);
			// ����ͼ������ִ��ˢ�º���Ч
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
		 * �����첽���ص�POI��ѯ�����������BDLocation���Ͳ���
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
            popupText.setText("[�ҵ�λ��]\n" + loc);
        else
            popupText.setText("[�ҵ�λ��]");
        pop.showPopup(getBitmapFromView(popupText),
                new GeoPoint((int) (location.getLatitude() * 1e6),
                        (int) (location.getLongitude() * 1e6)), 10);
    }
	// �̳�MyLocationOverlay��дdispatchTap����
	private class LocationOverlay extends MyLocationOverlay {

		public LocationOverlay(MapView arg0) {
			super(arg0);
		}

		/**
		 * �ڡ��ҵ�λ�á������ϴ������¼���
		 */
		@Override
		protected boolean dispatchTap() {
			// ����ҵ�λ����ʾPopupOverlay
			showPopupOverlay(location);
			return super.dispatchTap();
		}

	}

	private void refreshScaleAndZoomControl() {
		// �������Ű�ť��״̬
		mZoomControlView.refreshZoomButtonStatus(Math.round(mMapView
				.getZoomLevel()));
		mScaleView.refreshScaleView(Math.round(mMapView.getZoomLevel()));
	}

	@Override
	protected void onDestroy() {
		// MapView������������Activityͬ������activity����ʱ�����MapView.destroy()
		mMapView.destroy();
		
		mSearch.destory();

		// �˳�Ӧ�õ���BMapManager��destroy()����
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}

		// �˳�ʱ���ٶ�λ
		if (mLocClient != null) {
			mLocClient.stop();
		}

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// MapView������������Activityͬ������activity����ʱ�����MapView.onPause()
		mMapView.onPause();
		if (mBMapMan != null) {
			mBMapMan.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		// MapView������������Activityͬ������activity����ʱ�����MapView.onPause()
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