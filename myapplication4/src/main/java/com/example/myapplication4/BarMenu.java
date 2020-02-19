package com.example.myapplication4;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;

public class BarMenu {
    private JSONObject jObject;
    public BarMenu(String json) throws JSONException {
        super();
        jObject = new JSONObject(json);
        Iterator<String> keys = jObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Log.d("myLogs", key);
            JSONArray jsonArray = jObject.optJSONArray(key);

            if ( jsonArray != null ) {
                for (int i=0; i < jsonArray.length(); i++)
                {
                    try {
                        JSONObject oneObject = jsonArray.getJSONObject(i);
                        // Pulling items from the array
                        String oneObjectsItem = oneObject.getString("Наименование");
                        String oneObjectsItem2 = oneObject.getString("Цена");
                        Log.d("myLogs", oneObjectsItem);
                        Log.d("myLogs", oneObjectsItem2);
                    } catch (JSONException e) {
                        // Oops
                    }
                }
            }
        }
    }

    public Iterator<String> firstLevel()
    {
        return jObject.keys();
    }
}
