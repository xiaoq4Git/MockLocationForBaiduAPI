package com.baidu.location.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import android.os.Environment;

public class SaveSDCardService {

	public SaveSDCardService() {
		
	}
	
	private Calendar date = Calendar.getInstance();

	public boolean write(String context) {
		File file = new File(Environment.getExternalStorageDirectory(), "/Gpsdata/record/" + date.get(Calendar.YEAR)
				+ "-" + (date.get(Calendar.MONTH) + 1) + "-" + date.get(Calendar.DAY_OF_MONTH) + ".txt");
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			try {
				FileOutputStream fos = new FileOutputStream(file, true);
				fos.write(context.getBytes());
				fos.close();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			// 此时SDcard不存在或者不能进行读写操作的
			return false;
		}
	}
}
