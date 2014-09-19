package com.example.knoaaccessibility;

import java.util.ArrayList;
import java.util.List;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityRecord;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressLint("NewApi")
public class KnoaAccessibilityService extends AccessibilityService {
	private static final String EVENT_TABLE = "Event";
	private static final String EVENT_RAW_DATA = "eventRawData";
	private static final String LOG_TAG = "Accessibility";
	private static final String EVENT_TYPE = "type";
	private static final String APPLICATION = "application";;
	private static final String CHILDREN = "children";
	private static final String TEXT = "text";
	private static final String ELEMENT = "element";
	private static final String SOURCE_RAW_DATA = "sourceRawData";
	private static final String SOURCE_TABLE = "Source";
	private static final String EVENT_NAME = "eventName";
	private static final String EVENT_ID = "eventId";
	private static final String HASH_CODE = "hashCode";
	private static final String DESCRIPTION = "description";
	private static final String KEY_EVENT_TABLE = "EventKey";
	private static final String EVENT_KEY = "event";
	private static final String RECORDS_KEY = "records";
	private static final String ALL_CHILDREN_KEY = "allChildren";
	private static final String ANCESTORS_KEY = "ancestors";

	private String mEventId;

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		
		Log.d(LOG_TAG, "onAccessibilityEvent");

		try {
			//Save the event details
			saveEvent(event);
			
			AccessibilityNodeInfo source = event.getSource();
			
			if (source == null) {
				return;
			}
			//Save the source and its properties.
			String srcId = saveSource(source, event);
			//Save the records of the event. 
			//contains information about state change of its source View.
			saveRecords(event, mEventId);
			// Save the ancestors of the view that fired the event.
			getAncestors(source, srcId);
			// Save all children of the view that fired the event.
			saveAllChildren(source);

		} catch (Exception e) {

			Log.e(LOG_TAG, e.toString() + "," + e.getStackTrace());
		}

	}

	@Override
	public void onInterrupt() {
		Log.e(LOG_TAG, "onInterrupt");

	}

	@Override
	public void onCreate() {
		Log.d(LOG_TAG, "onCreate");
		Parse.initialize(this, "9Dnom3GmCau1ZuNmbgfICztiCHSa09t1tTwGUdDX",
				"fXcUcMO6ssCLmPsroleCcf4wV78QnZVzEX8OwP34");
	}

	@Override
	protected boolean onKeyEvent(KeyEvent event) {

		// API 18 - Build.VERSION_CODES.JELLY_BEAN_MR2

		// Callback that allows an accessibility service to observe the key
		// events
		// before they are passed to the rest of the system. This means that the
		// events are first delivered here before they are passed to the device
		// policy, the input method, or applications.

		Log.d(LOG_TAG, "onKeyEvent:" + event.toString());
		ParseObject eventOBj = new ParseObject(KEY_EVENT_TABLE);
		eventOBj.put(EVENT_KEY, event.toString());
		try {
			eventOBj.save();
		} catch (ParseException e) {
			Log.e(LOG_TAG, "onKeyEvent:" + e.toString());
		}
		return false;
	}

	private String getEventText(AccessibilityEvent event) {
		StringBuilder sb = new StringBuilder();
		for (CharSequence s : event.getText()) {
			sb.append(s);
		}
		return sb.toString();
	}

	private AccessibilityNodeInfo getRoot(String eventId) {
		// Gets the root node in the currently active window if this
		// service can retrieve window content.
		// API 16+
		try {
			AccessibilityNodeInfo root = getRootInActiveWindow();
			updateParseObj(eventId, EVENT_TABLE, "root", root.toString());
			Log.d(LOG_TAG, "root: " + root.toString());
			return root;
		} catch (Exception e) {
			Log.e(LOG_TAG, "Root : " + e.toString());
			return null;
		}
	}

	private void updateParseObj(String objectId, String table, String key,
			final String value) {

		ParseQuery<ParseObject> query = ParseQuery.getQuery(table);
		try {
			ParseObject obj = query.get(objectId);
			obj.put(key, value);
			obj.save();
		} catch (ParseException e) {
			Log.e(LOG_TAG, "updateParseObj : " + e.toString());
			e.printStackTrace();
		}

	}

	private String saveEvent(AccessibilityEvent event) {

		// Save in Parse and local storage
		try {
			ParseObject eventOBj = new ParseObject(EVENT_TABLE);
			eventOBj.put(EVENT_RAW_DATA, event.toString());
			eventOBj.put(EVENT_TYPE,
					AccessibilityEvent.eventTypeToString(event.getEventType()));
			eventOBj.put(APPLICATION, event.getPackageName());
			if (event.getContentDescription() != null) {
				eventOBj.put("contentDescription", event
						.getContentDescription().toString());
			}
			String eventText = getEventText(event);
			if (eventText != null) {
				eventOBj.put("text", eventText);
			}
			if (event.getParcelableData() != null) {
				String parcebleText = event.getParcelableData().toString();
				if (parcebleText != null)
					eventOBj.put("parcebleText", parcebleText);
			}
			// API 19+
			// eventOBj.put("ContentChangeTypes",
			// event.getContentChangeTypes());

			try {
				eventOBj.save();
				ExternalStorageHandler.WriteToLog(event.toString());
			} catch (ParseException ex) {
				Log.e(LOG_TAG, ex.toString());
			}
			// Log event
			Log.d(LOG_TAG, event.toString());
			mEventId = eventOBj.getObjectId();
			return eventOBj.getObjectId();

		} catch (Exception e) {
			Log.d(LOG_TAG, e.toString());
			return null;
		}

	}

	private String saveSource(AccessibilityNodeInfo source,
			AccessibilityEvent event) {
		ParseObject sourceOBj = new ParseObject(SOURCE_TABLE);
		sourceOBj.put(SOURCE_RAW_DATA, source.toString());
		sourceOBj.put(APPLICATION, source.getPackageName());
		sourceOBj.put(ELEMENT, source.getClassName());
		sourceOBj.put(TEXT, source.getText() == null ? "" : source.getText()
				.toString());
		sourceOBj.put(EVENT_NAME,
				AccessibilityEvent.eventTypeToString(event.getEventType()));
		sourceOBj.put(EVENT_ID, mEventId);
		sourceOBj.put(DESCRIPTION, source.getContentDescription() == null ? ""
				: source.getContentDescription());
		sourceOBj.put(HASH_CODE, Integer.toHexString(source.hashCode()));
		Log.d(LOG_TAG, "source.isVisibleToUser() :" + source.isVisibleToUser());
		sourceOBj.put("isVisibleToUser", source.isVisibleToUser());

		AccessibilityNodeInfo findFocusAccessibility = source
				.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
		if (findFocusAccessibility != null)
			sourceOBj.put("findFocusAccessibility",
					findFocusAccessibility.toString());

		AccessibilityNodeInfo findFocusInput = source
				.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
		if (findFocusInput != null)
			sourceOBj.put("findFocusInput", findFocusInput.toString());
		// API 17
		// AccessibilityNodeInfo labeledBy = source.getLabeledBy();
		// if (labeledBy != null) {
		// sourceOBj.put("LabeledBy", labeledBy.toString());
		// Log.d(LOG_TAG, "LabeledBy :" + labeledBy.toString());
		// }
		// AccessibilityNodeInfo labelFor = source.getLabelFor();
		// if (labelFor != null) {
		// sourceOBj.put("LabelFor", labelFor.toString());
		// Log.d(LOG_TAG, "labelFor :" + labelFor.toString());
		// }
		// API 18

		// Gets the fully qualified resource name of the source view's id.
		// if (source.getViewIdResourceName() != null) {
		// sourceOBj.put("ViewIdResourceName", source.getViewIdResourceName());
		// Log.d(LOG_TAG,
		// "ViewIdResourceName :" + source.getViewIdResourceName());
		// }
		// // API 19
		// sourceOBj.put("inputType", source.getInputType());
		// if (source.getExtras() != null) {
		// Log.d(LOG_TAG, "extras :" + source.getExtras().toString());
		// sourceOBj.put("extras", source.getExtras().toString());
		// }

		try {
			sourceOBj.save();
			Log.d(LOG_TAG, "source:" + source.toString());
			ExternalStorageHandler.WriteToLog(source.toString());
			return sourceOBj.getObjectId();
		} catch (ParseException ex) {
			Log.e(LOG_TAG, ex.toString());
			return null;
		}

	}

	private List<AccessibilityNodeInfo> getAncestors(
			AccessibilityNodeInfo source, String sourceId) {
		AccessibilityNodeInfo current = source;
		List<AccessibilityNodeInfo> ancestors = new ArrayList<AccessibilityNodeInfo>();

		while (true) {
			AccessibilityNodeInfo parent = current.getParent();
			if (parent == null) {
				Log.d(LOG_TAG, "ancestors:" + ancestors.toString());
				updateParseObj(sourceId, SOURCE_TABLE, ANCESTORS_KEY,
						ancestors.toString());
				return null;
			}
			ancestors.add(parent);
			AccessibilityNodeInfo oldCurrent = current;
			current = parent;
			oldCurrent.recycle();
		}
	}

	private List<AccessibilityNodeInfo> getChildren(AccessibilityNodeInfo source) {
		List<AccessibilityNodeInfo> children = new ArrayList<AccessibilityNodeInfo>();
		int childrenNum = source.getChildCount();
		for (int i = 0; i < childrenNum; i++) {
			AccessibilityNodeInfo child = source.getChild(i);
			children.add(child);
		}
		return children;
	}

	private List<AccessibilityNodeInfo> saveAllChildren(
			AccessibilityNodeInfo source) {
		if (source == null) {

			return new ArrayList<AccessibilityNodeInfo>();
		}
		List<AccessibilityNodeInfo> allSubchilden = new ArrayList<AccessibilityNodeInfo>();

		List<AccessibilityNodeInfo> children = getChildren(source);
		allSubchilden.addAll(children);
		for (AccessibilityNodeInfo child : children) {

			allSubchilden.addAll(saveAllChildren(child));
		}
		return allSubchilden;
	}

	private List<AccessibilityRecord> saveRecords(AccessibilityEvent event,
			String eventId) {
		/*
		 * Represents a record in an AccessibilityEvent and contains information
		 * about state change of its source View. When a view fires an
		 * accessibility event it requests from its parent to dispatch the
		 * constructed event. The parent may optionally append a record for
		 * itself for providing more context to AccessibilityServices. Hence,
		 * accessibility services can facilitate additional accessibility
		 * records to enhance feedback.
		 * 
		 * Once the accessibility event containing a record is dispatched the
		 * record is made immutable and calling a state mutation method
		 * generates an error.
		 */
		try {
			List<AccessibilityRecord> records = new ArrayList<AccessibilityRecord>();
			int recordsCount = event.getRecordCount();
			Log.e(LOG_TAG, "record count:" + recordsCount);
			for (int i = 0; i < recordsCount; i++) {
				AccessibilityRecord record = event.getRecord(i);
				records.add(record);
				Log.e(LOG_TAG, "record " + i + " : " + record.toString());

			}
			updateParseObj(eventId, EVENT_TABLE, RECORDS_KEY,
					records.toString());
			return records;

		} catch (Exception e) {
			Log.e(LOG_TAG,
					"getRecords: " + e.toString() + "\n " + e.getStackTrace());
			return null;
		}

	}

}
