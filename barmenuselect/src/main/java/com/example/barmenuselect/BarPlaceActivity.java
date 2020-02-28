package com.example.barmenuselect;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.barmenuselect.BarOrder.TAG_ID;
import static com.example.barmenuselect.BarOrder.TAG_NAME;
import static com.example.barmenuselect.BarOrder.TAG_MENU_PERMITION;
import static com.example.barmenuselect.BarOrder.TAG_TABLE_ID;
import static com.example.barmenuselect.BarOrder.TAG_TABLE_NAME;

public class BarPlaceActivity extends AppCompatActivity implements View.OnClickListener, SimpleAdapter.ViewBinder, AdapterView.OnItemClickListener {
    private static final String TAG_MENU_TABLE = "table";
    private static final String TAG_MENU_SEAT = "seat";
    private ArrayList<Map<String, Object>> data;
    private ListView lvMain;
    private SimpleAdapter simpleAdapter;
    private String manId;
    private String manName;
    private String manPermition;
    private ArrayList<BarOrder> listBarOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_start);
        lvMain = findViewById(R.id.lvMain);
        listBarOrder = new ArrayList<>();

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {TAG_ID, TAG_NAME, TAG_MENU_TABLE, TAG_MENU_SEAT};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = {R.id.tvId, R.id.tvName, R.id.tvTable, R.id.tvSeat};

        data = new ArrayList<Map<String, Object>>();
        simpleAdapter = new SimpleAdapter(this, data, R.layout.place_item, from, to);
        simpleAdapter.setViewBinder(this);

        lvMain.setAdapter(simpleAdapter);
        lvMain.setOnItemClickListener(this);

        Intent intent = getIntent();
        manId = intent.getStringExtra(TAG_ID);
        manName = intent.getStringExtra(TAG_NAME);
        manPermition = intent.getStringExtra(TAG_MENU_PERMITION);

        lvMain.addHeaderView(createManHeader());
        refresh();
    }

    private View createManHeader() {
        View v = getLayoutInflater().inflate(R.layout.man_item, null);
        v.setBackgroundColor(Color.parseColor("#DCEDC8"));
        ((TextView) v.findViewById(R.id.tvId)).setText(manId);
        ((TextView) v.findViewById(R.id.tvName)).setText(manName);
        ((TextView) v.findViewById(R.id.tvTable)).setText(manPermition);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bnRefresh:
                refresh();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }

    void refresh() {
        LoadDataTask loadDataTask = new LoadDataTask();
        loadDataTask.execute();
    }

    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (id < 0) {
            return;
        }
        Intent intent = new Intent(this, BarOrderActivity.class);
        Map<String, Object> item = data.get((int) id);
        intent.putExtra(TAG_ID, manId);
        intent.putExtra(TAG_NAME, manName);
        intent.putExtra(TAG_TABLE_ID, (String) item.get(TAG_ID));
        intent.putExtra(TAG_TABLE_NAME, (String) item.get(TAG_NAME));

        startActivity(intent);
    }

    class LoadDataTask extends AsyncTask<Void, Void, Void> {

        private void LoadMan() {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://178.46.165.64:3306/bar", "Alexander", "vertex77");

                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("select * from place ");
                ResultSetMetaData rsmd = rs.getMetaData();

                data.clear();
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<String, Object>();

                    item.put(TAG_ID, rs.getString(1));
                    item.put(TAG_NAME, rs.getString(2));
                    item.put(TAG_MENU_TABLE, rs.getString(3));
                    item.put(TAG_MENU_SEAT, rs.getString(4));

                    data.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            LoadMan();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            simpleAdapter.notifyDataSetChanged();
        }
    }

    class LoadOrderListTask extends AsyncTask<Void, Void, Void> {

        private void LoadMan() {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://178.46.165.64:3306/bar", "Alexander", "vertex77");

                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("select * from order_list order by id desc");
                ResultSetMetaData rsmd = rs.getMetaData();

                listBarOrder.clear();
                if (rs.next()) {
                    JSONObject jsonObject = new JSONObject(rs.getString(2));
                    JSONArray names = jsonObject.names();
                    for (int i = 0; i < names.length(); ++i) {
                        String name = names.getString(i);
                        BarOrder item = new BarOrder(name, jsonObject.getJSONObject(name));

                        listBarOrder.add(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            LoadMan();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            simpleAdapter.notifyDataSetChanged();
        }
    }
}
