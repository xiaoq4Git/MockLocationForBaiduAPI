package com.codoon.location.demo;

import java.util.Random;
import java.util.concurrent.ExecutorService;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.codoon.baidulocationdemo.R;
import com.codoon.location.service.LoncationKeepAlive;
import com.codoon.location.service.SaveSDCardService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/***
 * 单点定位示例，用来展示基本的定位结果，配置在LocationService.java中 默认配置也可以在LocationService中修改
 * 
 * @author baidu
 *
 */
public class LocationRecordActivity extends Activity {
	private SaveSDCardService saveSDCardService;
	private TextView LocationResult;
	private Button startLocation;
	private ExecutorService cachedThreadPool;
	private double lastKnowLat = 0;
	private double lastKnowLon = 0;
	private StringBuffer sb, sb1;
	public static final String ACTION_UPDATEUI = "action.updateUI";
	UpdateUIBroadcastReceiver broadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// -----------demo view config ------------
		setContentView(R.layout.location);
		LocationResult = (TextView) findViewById(R.id.textView1);
		LocationResult.setMovementMethod(ScrollingMovementMethod.getInstance());
		startLocation = (Button) findViewById(R.id.addfence);
	}

	private class UpdateUIBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			LocationResult.setText(intent.getExtras().getString("count"));
		}
	}

	/**
	 * 显示请求字符串
	 * 
	 * @param str
	 */
	public void logMsg(String str) {
		try {
			if (LocationResult != null)
				LocationResult.setText(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***
	 * Stop location service
	 */
	@Override
	protected void onStop() {
		// locationService.unregisterListener(mListener); //注销掉监听
		// locationService.stop(); //停止定位服务
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();

		startLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LocationRecordActivity.this, LoncationKeepAlive.class);
				if (startLocation.getText().toString().equals(getString(R.string.startlocation))) {
					startLocation.setText(getString(R.string.stoplocation));
					intent.putExtra("FLAG_RUN", "START");
					
			        IntentFilter filter = new IntentFilter();  
			        filter.addAction(ACTION_UPDATEUI);  
			        broadcastReceiver = new UpdateUIBroadcastReceiver();  
			        registerReceiver(broadcastReceiver, filter);
				} else {
					startLocation.setText(getString(R.string.startlocation));
					intent.putExtra("FLAG_RUN", "STOP");
				}
				startService(intent);
			}
		});
	}

	/*****
	 * @see copy funtion to you project
	 *      定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
	 *
	 */
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
				logMsg(sb.toString());

				if (location.getLocType() == BDLocation.TypeServerError
						&& location.getLocType() == BDLocation.TypeNetWorkException
						&& location.getLocType() == BDLocation.TypeCriteriaException) {
					return;
				}

				if (location.getLatitude() != lastKnowLat && location.getLongitude() != lastKnowLon) {
					sb1.append(
							"\t<wpt lat=\"" + location.getLatitude() + "\" lon=\"" + location.getLongitude() + "\">\n");
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
