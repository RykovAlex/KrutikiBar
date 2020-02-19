package com.example.barmenuselect;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, SimpleAdapter.ViewBinder {
    BarMenu barMenu;
    ListView lvMenu;
    ArrayList<Map<String, Object>> data;
    SimpleAdapter simpleAdapter;

    final String TAG_MENU_NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvMenu = findViewById(R.id.lvMenu);

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {TAG_MENU_NAME};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = {R.id.tvName};

        data = new ArrayList<Map<String, Object>>();
        simpleAdapter = new SimpleAdapter(this, data, R.layout.menu_item, from, to);
        //simpleAdapter.setViewBinder(this);

        lvMenu.setAdapter(simpleAdapter);
        lvMenu.setOnItemClickListener(this);

        LoadMenuTask loadMenuTask = new LoadMenuTask();
        loadMenuTask.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        data.clear();
        data.addAll(barMenu.getLevel(position));
        simpleAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation) {
        ((TextView) view).setText(textRepresentation);
        return true;
    }

    class LoadMenuTask extends AsyncTask<Void, Void, Void> {

        private void LoadMenu() {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://178.46.165.64:3306/bar", "Alexander", "vertex77");

                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("select * from menu ");
                ResultSetMetaData rsmd = rs.getMetaData();

                while (rs.next()) {
                    barMenu = new BarMenu(rs.getString(2));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            LoadMenu();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            data.addAll(barMenu.getLevel(null));
            simpleAdapter.notifyDataSetChanged();
        }
    }

    private class BarMenu {
        private JSONObject jObject;
        private JSONArray jCurArray;
        private JSONObject jCurObject;

        public BarMenu(String jsonStr) {
            super();
            try {
                jObject = new JSONObject(jsonStr);
                Iterator<String> keys = jObject.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    Log.d("myLogs", key);
                    JSONArray jsonArray = jObject.optJSONArray(key);

                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject oneObject = jsonArray.getJSONObject(i);
                                // Pulling items from the array
                                String oneObjectsItem = oneObject.getString("Наименование");
                                String oneObjectsItem2 = oneObject.getString("Цена");
                            } catch (JSONException e) {
                                // Oops
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        void top() {
            jCurObject = jObject;
            jCurArray = null;
        }

        ArrayList<Map<String, Object>> getLevel(Integer index) {

            ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            Iterator<String> keys;

            if (index == null) {
                jCurObject = jObject;
                jCurArray = null;

                JSONArray names = jCurObject.names();
                for (int i = 0; i < names.length(); ++i) {
                    Map<String, Object> item = new HashMap<String, Object>();

                    try {
                        item.put(TAG_MENU_NAME, names.getString(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    result.add(item);
                }
            } else {
                if (jCurArray != null) {

                }
                if (jCurObject != null) {
                    JSONArray names = jCurObject.names();
                    try {
                        jCurArray = jCurObject.optJSONArray(names.getString(index.intValue()));
                        if (jCurArray != null) {
                            for (int i = 0; i < jCurArray.length(); ++i) {
                                Map<String, Object> item = new HashMap<String, Object>();

                                try {
                                    JSONObject jsonObject = jCurArray.optJSONObject(i);
                                    if (jsonObject != null) {
                                        item.put(TAG_MENU_NAME, jsonObject.getString("Наименование") +" "+ jsonObject.getString("Единица"));
                                    } else {
                                        JSONArray jsonArray = jCurArray.optJSONArray(i);
                                        if (jsonArray != null) {

                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                result.add(item);
                            }
                        }
                        jCurObject = jCurObject.optJSONObject(names.getString(index.intValue()));
                        if (jCurObject != null) {
                            names = jCurObject.names();
                            for (int i = 0; i < names.length(); ++i) {
                                Map<String, Object> item = new HashMap<String, Object>();

                                try {
                                    item.put(TAG_MENU_NAME, names.getString(i));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                result.add(item);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            }

            return result;
        }
    }
}
