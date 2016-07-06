package com.codoon.location.service;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.codoon.baidulocationdemo.R;
import com.codoon.location.demo.LocationApplication;
import com.codoon.location.demo.LocationRecordActivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class LoncationKeepAlive extends Service {
	private LocationService locationService;
	private SaveSDCardService saveSDCardService;
	private ExecutorService cachedThreadPool;
	private Timer timer;
	private double lastKnowLat = 0;
	private double lastKnowLon = 0;
	private static String STATUS = null;
	private StringBuffer sb = null;
	private StringBuffer sb1 = null;
	private Intent intent;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		cachedThreadPool = Executors.newCachedThreadPool();
		timer = new Timer();
		intent = new Intent();

		// -----------location config ------------
		locationService = ((LocationApplication) getApplication()).locationService;
		Log.i("onCreate", "初始化locationService实例");
		// 获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
		locationService.registerListener(mListener);
		Log.i("onCreate", "locationService注册监听");
		// 注册监听
		saveSDCardService = ((LocationApplication) getApplication()).saveSDCardService;
		Log.i("onCreate", "初始化saveSDCardService实例");
		// 获取saveSDCardService实例
		locationService.setLocationOption(locationService.getDefaultLocationClientOption());
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_STICKY;

		Notification notification = new Notification(R.drawable.ic_launcher, getString(R.string.app_name),
				System.currentTimeMillis());
		Intent intent2 = new Intent(this, LocationRecordActivity.class);

		PendingIntent pendingintent = PendingIntent.getActivity(this, 0, intent2, 0);
		notification.setLatestEventInfo(this, "GPS采点模块", "Running...", pendingintent);
		startForeground(0x111, notification);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		STATUS = intent.getStringExtra("FLAG_RUN");
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (STATUS.equals("START")) {
					Log.i("onStart", "locationService is running...");
					locationService.start();
				} else {
					Log.i("onStart", "locationService is stop");
					locationService.stop();
				}
			}
		}, 0, 10 * 1000);

		super.onStart(intent, startId);
	}

	private BDLocationListener mListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (null != location && location.getLocType() != BDLocation.TypeServerError) {
				sb = new StringBuffer(256);
				sb1 = new StringBuffer(128);
				sb.append("time : ");
				/**
				 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
				 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
				 */
				sb.append(location.getTime());
				sb.append("\nerror code : ");
				sb.append(location.getLocType());
				sb.append("\nlatitude : ");
				sb.append(location.getLatitude());
				sb.append("\nlontitude : ");
				sb.append(location.getLongitude());
				sb.append("\nradius : ");
				sb.append(location.getRadius());
				sb.append("\nCountryCode : ");
				sb.append(location.getCountryCode());
				sb.append("\nCountry : ");
				sb.append(location.getCountry());
				sb.append("\ncitycode : ");
				sb.append(location.getCityCode());
				sb.append("\ncity : ");
				sb.append(location.getCity());
				sb.append("\nDistrict : ");
				sb.append(location.getDistrict());
				sb.append("\nStreet : ");
				sb.append(location.getStreet());
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append("\nDescribe: ");
				sb.append(location.getLocationDescribe());
				sb.append("\nDirection(not all devices have value): ");
				sb.append(location.getDirection());
				sb.append("\nPoi: ");
				if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
					for (int i = 0; i < location.getPoiList().size(); i++) {
						Poi poi = (Poi) location.getPoiList().get(i);
						sb.append(poi.getName() + ";");
					}
				}
				if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
					sb.append("\nspeed : ");
					sb.append(location.getSpeed());// 单位：km/h
					sb.append("\nsatellite : ");
					sb.append(location.getSatelliteNumber());
					sb.append("\nheight : ");
					sb.append(location.getAltitude());// 单位：米
					sb.append("\ndescribe : ");
					sb.append("gps定位成功");
				} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
					sb.append("\noperationers : ");
					sb.append(location.getOperators());
					sb.append("\ndescribe : ");
					sb.append("网络定位成功");
				} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
					sb.append("\ndescribe : ");
					sb.append("离线定位成功，离线定位结果也是有效的");
				} else if (location.getLocType() == BDLocation.TypeServerError) {
					sb.append("\ndescribe : ");
					sb.append("服务端网络定位失败");
				} else if (location.getLocType() == BDLocation.TypeNetWorkException) {
					sb.append("\ndescribe : ");
					sb.append("网络不同导致定位失败，请检查网络是否通畅");
				} else if (location.getLocType() == BDLocation.TypeCriteriaException) {
					sb.append("\ndescribe : ");
					sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
				}

				intent.setAction(LocationRecordActivity.ACTION_UPDATEUI);
				intent.putExtra("count", sb.toString());
				sendBroadcast(intent);

				if (location.getLocType() == BDLocation.TypeServerError
						&& location.getLocType() == BDLocation.TypeNetWorkException
						&& location.getLocType() == BDLocation.TypeCriteriaException) {
					return;
				}

				if (location.getLatitude() != lastKnowLat && location.getLongitude() != lastKnowLon) {
					sb1.append(
							"\t<wpt lat=\"" + location.getLatitude() + "\" lon=\"" + location.getLongitude() + "\">\n");
					sb1.append("\t\t<time>" + location.getTime() + "</time>\n");
					if (location.getAltitude() < 1) {
						Random rad = new Random();
						sb1.append("\t\t<ele>" + (rad.nextInt(100) + 300) + "</ele>\n");
					} else {
						sb1.append("\t\t<ele>" + location.getAltitude() + "</ele>\n");
					}
					sb1.append("\t</wpt>\n");

					cachedThreadPool.execute(new Runnable() {

						@Override
						public void run() {
							saveSDCardService.write(sb1.toString());
						}
					});
				}
				lastKnowLat = location.getLatitude();
				lastKnowLon = location.getLongitude();
			}
		}
	};
}
