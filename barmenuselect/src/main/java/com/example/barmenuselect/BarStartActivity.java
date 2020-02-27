package com.example.barmenuselect;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

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

public class BarStartActivity extends AppCompatActivity implements View.OnClickListener, SimpleAdapter.ViewBinder, AdapterView.OnItemClickListener {

    private ArrayList<Map<String, Object>> data;
    private ListView lvMan;
    private SimpleAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_start);
        lvMan = findViewById(R.id.lvMain);

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {TAG_ID, TAG_NAME, TAG_MENU_PERMITION};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = { R.id.tvId, R.id.tvName, R.id.tvTable};

        data = new ArrayList<Map<String, Object>>();
        simpleAdapter = new SimpleAdapter(this, data, R.layout.man_item, from, to);
        simpleAdapter.setViewBinder(this);

        lvMan.setAdapter(simpleAdapter);
        lvMan.setOnItemClickListener(this);

        refresh();
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
        LoadManTask loadManTask = new LoadManTask();
        loadManTask.execute();
    }

    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, BarPlaceActivity.class);
        Map<String, Object> item = data.get(position);
        intent.putExtra(TAG_ID, (String)item.get(TAG_ID));
        intent.putExtra(TAG_NAME, (String)item.get(TAG_NAME));
        intent.putExtra(TAG_MENU_PERMITION, (String)item.get(TAG_MENU_PERMITION));

        startActivity(intent);
    }

    class LoadManTask extends AsyncTask<Void, Void, Void> {

        private void LoadMan() {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://178.46.165.64:3306/bar", "Alexander", "vertex77");

                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("select * from man ");
                ResultSetMetaData rsmd = rs.getMetaData();

                data.clear();
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<String, Object>();

                    item.put(TAG_ID, rs.getString(1));
                    item.put(TAG_NAME, rs.getString(3));
                    item.put(TAG_MENU_PERMITION, rs.getString(4));

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

}
