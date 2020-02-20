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

    final String TAG_MENU_ID = "id";
    final String TAG_MENU_NAME = "name";
    final String TAG_MENU_UNIT = "unit";
    final String TAG_MENU_PRICE = "price";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvMenu = findViewById(R.id.lvMenu);

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {TAG_MENU_ID,TAG_MENU_NAME, TAG_MENU_UNIT, TAG_MENU_PRICE};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = {R.id.tvId,R.id.tvName,R.id.tvUnit,R.id.tvPrice};

        data = new ArrayList<Map<String, Object>>();
        simpleAdapter = new SimpleAdapter(this, data, R.layout.menu_item, from, to);
        //simpleAdapter.setViewBinder(this);

        lvMenu.setAdapter(simpleAdapter);
        lvMenu.setOnItemClickListener(this);

        LoadMenuTask loadMenuTask = new LoadMenuTask();
        loadMenuTask.execute();
    }

    // создание Header
    View createHeader(String name, String id) {
        View v = getLayoutInflater().inflate(R.layout.menu_header, null);
        ((TextView) v.findViewById(R.id.tvId)).setText(id);
        ((TextView) v.findViewById(R.id.tvName)).setText(name);
        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        data.clear();
        String index = ((TextView) view.findViewById(R.id.tvId)).getText().toString();
        if (id < 0) {
            data.addAll(barMenu.upLevel(index));
        }else {
            data.addAll(barMenu.getLevel(index));
        }
        simpleAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation) {
        //((TextView) view).setText(textRepresentation);
        return false;
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
        private JSONObject jCurObject;
        private ArrayList<String> selectedKeys;
        private HashMap<String, JSONObject> maps;
        private HashMap<String, View> headers;

        public BarMenu(String jsonStr) {
            super();
            try {
                jObject = new JSONObject(jsonStr);
                jCurObject = jObject;

                selectedKeys = new ArrayList<>();
                maps = new HashMap<>();
                headers = new HashMap<>();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        void clearHeaders(String index){
            int i = selectedKeys.indexOf(index);
            while (selectedKeys.size() != i){
                lvMenu.removeHeaderView(headers.get( selectedKeys.get(i) ));
                selectedKeys.remove(i);
            }
        }

        ArrayList<Map<String, Object>> getLevel(String index) {

            ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

            if (index == null) {
                if (jCurObject != null) {
                    JSONArray names = jCurObject.names();
                    for (int i = 0; i < names.length(); ++i) {
                        Map<String, Object> item = new HashMap<String, Object>();

                        try {
                            String namesString = names.getString(i);

                            if ( null == jCurObject.optJSONObject(namesString)) {
                                continue;
                            }

                            item.put(TAG_MENU_ID, namesString);
                            item.put(TAG_MENU_NAME, jCurObject.getJSONObject(namesString).optString("Наименование"));
                            item.put(TAG_MENU_UNIT, jCurObject.getJSONObject(namesString).optString("Единица")  );
                            item.put(TAG_MENU_PRICE, jCurObject.getJSONObject(namesString).optString("Цена"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        result.add(item);
                    }
                }
            } else {
                try {
                    View view = null;

                    view = createHeader( jCurObject.getJSONObject(index).optString("Наименование"), index );
                    headers.put(index, view);
                    selectedKeys.add(index);

                    lvMenu.addHeaderView(view);
                    maps.put(index, jCurObject);

                    jCurObject = jCurObject.optJSONObject(index);
                    if (jCurObject != null) {
                        result = getLevel(null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }

        ArrayList<Map<String, Object>> upLevel(String index) {

            clearHeaders(index);

            jCurObject = maps.get(index);
            maps.remove(index);

            return getLevel(null);
        }
    }
}
