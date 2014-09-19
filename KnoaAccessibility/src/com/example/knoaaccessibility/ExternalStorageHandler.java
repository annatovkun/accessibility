package com.example.knoaaccessibility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.os.Environment;
import android.util.Log;

public class ExternalStorageHandler {
	private static final String LOG_TAG = "Accessibility";

	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public static void InitLogFile() {

		String root = Environment.getExternalStorageDirectory().toString();
		File file = new File(root + "/knoaLogs/log.txt");
		if (file.exists())
			file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			Log.e(LOG_TAG, e.toString());
			e.printStackTrace();
		}
	}

	public static void WriteToLog(String log) {
		try {
			String root = Environment.getExternalStorageDirectory().toString();
			Log.i(LOG_TAG, "isExternalStorageWritable:"
					+ isExternalStorageWritable());
			File myDir = new File(root + "/knoaLogs");
			myDir.mkdirs();
			File file = new File(root + "/knoaLogs/log.txt");
			if (!file.exists())
				file.createNewFile();
			if (file.length() > 40000000) {
				InitLogFile();
			}
			Log.i(LOG_TAG, "saving to : " + root + "/knoaLogs/log.txt");
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
					fileOutputStream);

			outputStreamWriter.append(log);
			outputStreamWriter.close();
			fileOutputStream.close();
		} catch (IOException e) {
			Log.e(LOG_TAG, e.toString());
		}

	}
}
