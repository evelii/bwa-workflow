package ca.on.oicr.pde.seqprodreporter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.format.Time;
import android.util.Log;
import android.util.TimeFormatException;

public class JsonParser {

	private Time newUpdateTime;
	private List<Report> parsedJSON;

	public JsonParser(String JsonString, String[] types, Time lastUpdate) {
		this.parsedJSON = new ArrayList<Report>();
		this.newUpdateTime = null == lastUpdate ? null : new Time();
		try {
			JSONObject object = (JSONObject) new JSONTokener(JsonString)
					.nextValue();
			for (int t = 0; t < types.length; t++) {
			JSONArray jsonRecords = object.getJSONArray(types[t]);
			//Time newLatest = new Time();
			for (int i = 0; i < jsonRecords.length(); i++) {
				JSONObject tmp = (JSONObject) jsonRecords.get(i);

				String lmTime = tmp.getString("lmtime").replaceAll("-", ":")
						.replaceAll(":", "");
				boolean updated = false;

				// 2014-03-21 14:32:23.729
				try {
					String parsable = lmTime.substring(0,
							lmTime.lastIndexOf(".")).replace(" ", "T");
					Time recordTime = new Time();
					recordTime.parse(parsable);
					if (null != lastUpdate
							&& recordTime.after(lastUpdate))
						updated = true;
					if (null == this.newUpdateTime || this.newUpdateTime.before(recordTime))
						this.newUpdateTime = recordTime;

				} catch (TimeFormatException tfe) {
					Log.e(ReporterActivity.TAG,
							"An error with Parsing Time occured");
				}

				// TODO Need to use new data field entries once back end is modified
				Report newReport = new Report(tmp.getString("sample"),
						tmp.getString("workflow"), tmp.getString("version"),
						tmp.getString("crtime"), tmp.getString("lmtime"),
						tmp.getString("wrun_id"), updated);
				if (types[t].equals(ReporterActivity.types[2])
						&& tmp.has("progress")) {
					String p = tmp.getString("progress");
					if (null != p && !p.isEmpty())
						newReport.setrProgress(p);
				}

				this.parsedJSON.add(newReport);
			}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Time getNewUpdateTime() {
		return newUpdateTime;
	}

	public List<Report> getParsedJSON() {
		return parsedJSON;
	}

}
