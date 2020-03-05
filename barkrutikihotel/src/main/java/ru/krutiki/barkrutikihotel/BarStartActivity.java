package ru.krutiki.barkrutikihotel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.example.barkrutikihotel.R;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static ru.krutiki.barkrutikihotel.BarOrder.TAG_ID;
import static ru.krutiki.barkrutikihotel.BarOrder.TAG_MENU_PERMISSION;
import static ru.krutiki.barkrutikihotel.BarOrder.TAG_NAME;

public class BarStartActivity extends AppCompatActivity implements View.OnClickListener, SimpleAdapter.ViewBinder, AdapterView.OnItemClickListener {

    private ArrayList<Map<String, Object>> data;
    private SimpleAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_start);
        ListView lvMan = findViewById(R.id.lvMain);

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {TAG_ID, TAG_NAME, TAG_MENU_PERMISSION};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = {R.id.tvId, R.id.tvName, R.id.tvTable};

        data = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(this, data, R.layout.man_item, from, to);
        simpleAdapter.setViewBinder(this);

        lvMan.setAdapter(simpleAdapter);
        lvMan.setOnItemClickListener(this);

        refresh();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bnRefresh) {
            refresh();
        } else {
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
        intent.putExtra(TAG_ID, (String) item.get(TAG_ID));
        intent.putExtra(TAG_NAME, (String) item.get(TAG_NAME));
        intent.putExtra(TAG_MENU_PERMISSION, (String) item.get(TAG_MENU_PERMISSION));

        startActivity(intent);
    }

    @SuppressLint("StaticFieldLeak")
    class LoadManTask extends AsyncTask<Void, Void, Void> {

        private void LoadMan() {
            for (int i = 0; i < 2; ++i) {
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    DriverManager.setLoginTimeout(5);
                    Connection con = DriverManager.getConnection(BarOrder.getChannelIp(), getString(R.string.user_name), getString(R.string.user_password));

                    Statement st = con.createStatement();

                    ResultSet rs = st.executeQuery("select * from man ");

                    data.clear();
                    while (rs.next()) {
                        Map<String, Object> item = new HashMap<>();

                        item.put(TAG_ID, rs.getString(1));
                        item.put(TAG_NAME, rs.getString(3));
                        item.put(TAG_MENU_PERMISSION, rs.getString(4));

                        data.add(item);
                    }
                    break;

                } catch (Exception e) {
                    BarOrder.switchChannelIp();
                    e.printStackTrace();
                }
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
            if (getSupportActionBar() != null)
                getSupportActionBar().setSubtitle(BarOrder.getChannelName());

        }
    }

}
