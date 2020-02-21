package com.example.barmenuselect;

import android.graphics.Color;
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

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, SimpleAdapter.ViewBinder, View.OnClickListener {
    BarMenu barMenu;
    ListView lvMenu;
    ArrayList<Map<String, Object>> data;
    SimpleAdapter simpleAdapter;
    BarOrder barOrder;

    final String TAG_MENU_COLOR = "color";
    final String TAG_MENU_ID = "id";
    final String TAG_MENU_NAME = "name";
    final String TAG_MENU_UNIT = "unit";
    final String TAG_MENU_PRICE = "price";
    final String TAG_ORDER_COUNT = "count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvMenu = findViewById(R.id.lvMenu);

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = { TAG_MENU_ID, TAG_MENU_NAME, TAG_MENU_UNIT, TAG_MENU_PRICE, TAG_ORDER_COUNT, TAG_ORDER_COUNT, TAG_MENU_COLOR};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = {R.id.tvId, R.id.tvName, R.id.tvUnit, R.id.tvPrice, R.id.llCount, R.id.tvCount, R.id.llMain};

        data = new ArrayList<Map<String, Object>>();
        simpleAdapter = new SimpleAdapter(this, data, R.layout.menu_item, from, to);
        simpleAdapter.setViewBinder(this);

        lvMenu.setAdapter(simpleAdapter);
        lvMenu.setOnItemClickListener(this);

        barOrder = new BarOrder();

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
        String index = ((TextView) view.findViewById(R.id.tvId)).getText().toString();
        if (id < 0) {
            data.clear();
            data.addAll(barMenu.upLevel(index));
            simpleAdapter.notifyDataSetChanged();
        } else {
            String unit = ((TextView) view.findViewById(R.id.tvUnit)).getText().toString();
            if (!unit.isEmpty()) {
                barOrder.add(index);
                index = null;
            }
            refreshList(index);
        }
    }

    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation) {
        switch (view.getId()) {
            case R.id.llCount:
                int count = (int) data;
                if (count > 0) {
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
                return true;
            case R.id.llMain:
                String color = (String) data;
                if (!color.isEmpty()) {
                    view.setBackgroundColor(Color.parseColor(color));
                }
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        View rootLayout = (View) v.getParent().getParent();
        View layout = rootLayout.findViewById(R.id.llName);

        String index = ((TextView) layout.findViewById(R.id.tvId)).getText().toString();

        switch (v.getId()) {
            case R.id.ibDown:
                barOrder.remove(index);
                break;
            case R.id.ibUp:
                barOrder.add(index);
                break;
        }
        refreshList(null);
    }

    private void refreshList(String index) {
        data.clear();
        data.addAll(barMenu.getLevel(index));
        simpleAdapter.notifyDataSetChanged();
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

        void clearHeaders(String index) {
            int i = selectedKeys.indexOf(index);
            while (selectedKeys.size() != i) {
                lvMenu.removeHeaderView(headers.get(selectedKeys.get(i)));
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

                            if (null == jCurObject.optJSONObject(namesString)) {
                                continue;
                            }

                            item.put(TAG_MENU_ID, namesString);
                            item.put(TAG_MENU_COLOR, jCurObject.getJSONObject(namesString).optString("Цвет"));
                            item.put(TAG_MENU_NAME, jCurObject.getJSONObject(namesString).optString("Наименование"));
                            item.put(TAG_MENU_UNIT, jCurObject.getJSONObject(namesString).optString("Единица"));
                            item.put(TAG_MENU_PRICE, jCurObject.getJSONObject(namesString).optString("Цена"));
                            item.put(TAG_ORDER_COUNT, barOrder.getCount(namesString));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        result.add(item);
                    }
                }
            } else {
                try {
                    View view = null;

                    view = createHeader(jCurObject.getJSONObject(index).optString("Наименование"), index);
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
