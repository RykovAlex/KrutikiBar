package com.example.barmenuselect;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.barmenuselect.BarOrder.TAG_ID;
import static com.example.barmenuselect.BarOrder.TAG_INTENT_ORDER;
import static com.example.barmenuselect.BarOrder.TAG_JSON_MAN_ID;
import static com.example.barmenuselect.BarOrder.TAG_JSON_MAN_NAME;
import static com.example.barmenuselect.BarOrder.TAG_JSON_ORDER_AMOUNT;
import static com.example.barmenuselect.BarOrder.TAG_JSON_TABLE_ID;
import static com.example.barmenuselect.BarOrder.TAG_JSON_TABLE_NAME;
import static com.example.barmenuselect.BarOrder.TAG_MENU_PERMISSION;
import static com.example.barmenuselect.BarOrder.TAG_NAME;

public class BarPlaceActivity extends AppCompatActivity implements View.OnClickListener, SimpleAdapter.ViewBinder, AdapterView.OnItemClickListener {
    private static final String TAG_MENU_TABLE = "table";
    private static final String TAG_MENU_SEAT = "seat";
    private static final String TAG_ORDER_NUMBER = "order_number";
    private static final String TAG_ORDER_MAN = "order_man";
    private ArrayList<Map<String, Object>> data;
    private SimpleAdapter simpleAdapter;
    private String manId;
    private String manName;
    private String manPermission;
    private ArrayList<BarOrder> listBarOrder;
    private Timer timer = new Timer();
    private final Handler handler = new Handler();
    private String lastTimestamp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_start);
        ListView lvMain = findViewById(R.id.lvMain);
        listBarOrder = new ArrayList<>();

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {TAG_ID, TAG_NAME, TAG_MENU_TABLE, TAG_MENU_SEAT, TAG_ORDER_NUMBER, TAG_ORDER_MAN, TAG_ORDER_NUMBER, TAG_JSON_ORDER_AMOUNT};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = {R.id.tvId, R.id.tvName, R.id.tvTable, R.id.tvSeat, R.id.tvOrderNumber, R.id.tvOrderName, R.id.llOrder, R.id.tvSum};

        data = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(this, data, R.layout.place_item, from, to);
        simpleAdapter.setViewBinder(this);

        lvMain.setAdapter(simpleAdapter);
        lvMain.setOnItemClickListener(this);

        Intent intent = getIntent();
        manId = intent.getStringExtra(TAG_ID);
        manName = intent.getStringExtra(TAG_NAME);
        manPermission = intent.getStringExtra(TAG_MENU_PERMISSION);

        lvMain.addHeaderView(createManHeader(lvMain));
        refresh();
    }

    private BarOrder findOrder(String tableId) {
        for (BarOrder barOrder :
                listBarOrder) {
            if (barOrder.getTableId().equals(tableId)) {
                return barOrder;
            }
        }
        return null;
    }

    private View createManHeader(ListView parent) {
        View v = getLayoutInflater().inflate(R.layout.man_item, parent, false);
        v.setBackgroundColor(Color.parseColor("#DCEDC8"));
        ((TextView) v.findViewById(R.id.tvId)).setText(manId);
        ((TextView) v.findViewById(R.id.tvName)).setText(manName);
        ((TextView) v.findViewById(R.id.tvTable)).setText(manPermission);
        return v;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bnRefresh) {
            refresh();
        } else {
            throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }

    private boolean LoadListOrder() {
        boolean result = false;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://178.46.165.64:3306/bar", "Alexander", "vertex77");

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("select * from order_list order by id desc limit 1");

            if (rs.next()) {
                if (!lastTimestamp.equals(rs.getString(1))) {
                    listBarOrder.clear();
                    JSONObject jsonObject = new JSONObject(rs.getString(2));
                    JSONArray names = jsonObject.names();
                    for (int i = 0; i < names.length(); ++i) {
                        String name = names.getString(i);
                        BarOrder item = new BarOrder(name, jsonObject.getJSONObject(name));

                        listBarOrder.add(item);
                    }
                    lastTimestamp = rs.getString(1);
                    result = true;
                }
            } else {
                if (!lastTimestamp.equals("empty")) {
                    listBarOrder.clear();
                    lastTimestamp = "empty";
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void LoadPlace() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://178.46.165.64:3306/bar", "Alexander", "vertex77");

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("select * from place ");

            data.clear();
            int acceptedOrderCount = 0;
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();

                String tableId = rs.getString(1);
                item.put(TAG_ID, tableId);
                item.put(TAG_NAME, rs.getString(2));
                item.put(TAG_MENU_TABLE, rs.getString(3));
                item.put(TAG_MENU_SEAT, rs.getString(4));

                BarOrder barOrder = findOrder(tableId);
                if (barOrder == null) {
                    item.put(TAG_ORDER_NUMBER, "");
                    item.put(TAG_ORDER_MAN, "");
                    data.add(item);
                } else {
                    item.put(TAG_ORDER_NUMBER, barOrder.getNumber());
                    item.put(TAG_ORDER_MAN, barOrder.getManName());
                    item.put(TAG_JSON_ORDER_AMOUNT, barOrder.getAmount());
                    data.add(acceptedOrderCount++, item);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void refresh() {
//        LoadTask loadDataTask = new LoadTask();
//        loadDataTask.execute();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (LoadListOrder()) {
                            LoadPlace();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    simpleAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
                , 200, 3L * 1000);
    }

    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation) {
        if (view.getId() == R.id.llOrder) {
            String number = (String) data;
            view.setVisibility((number.isEmpty()) ? View.GONE : View.VISIBLE);
            return true;
        } else if (view.getId() == R.id.tvSum) {
            if (data == null) {
                view.setVisibility( View.GONE );
            } else {
                double amount = (double) data;
                String formattedDouble = String.format(Locale.US, "%.02f", amount);
                ((TextView) view).setText(formattedDouble);
                view.setVisibility( View.VISIBLE );
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (id < 0) {
            return;
        }
        Intent intent = new Intent(this, BarOrderActivity.class);
        Map<String, Object> item = data.get((int) id);
        BarOrder barOrder = findOrder((String) item.get(TAG_ID));
        if (barOrder == null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(TAG_JSON_MAN_ID, manId);
                jsonObject.put(TAG_JSON_MAN_NAME, manName);
                jsonObject.put(TAG_JSON_TABLE_ID, item.get(TAG_ID));
                jsonObject.put(TAG_JSON_TABLE_NAME, item.get(TAG_NAME));

                intent.putExtra(TAG_INTENT_ORDER, jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            intent.putExtra(TAG_INTENT_ORDER, barOrder.toString());
        }

        startActivityForResult(intent, 0);
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refresh();
    }
//    @SuppressLint("StaticFieldLeak")
//    class LoadTask extends AsyncTask<Void, Void, Void> {
//        private void LoadListOrder() {
//            try {
//                Class.forName("com.mysql.jdbc.Driver");
//                Connection con = DriverManager.getConnection("jdbc:mysql://178.46.165.64:3306/bar", "Alexander", "vertex77");
//
//                Statement st = con.createStatement();
//
//                ResultSet rs = st.executeQuery("select * from order_list order by id desc");
//
//                listBarOrder.clear();
//                if (rs.next()) {
//                    JSONObject jsonObject = new JSONObject(rs.getString(2));
//                    JSONArray names = jsonObject.names();
//                    for (int i = 0; i < names.length(); ++i) {
//                        String name = names.getString(i);
//                        BarOrder item = new BarOrder(name, jsonObject.getJSONObject(name));
//
//                        listBarOrder.add(item);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        private void LoadPlace() {
//            try {
//                Class.forName("com.mysql.jdbc.Driver");
//                Connection con = DriverManager.getConnection("jdbc:mysql://178.46.165.64:3306/bar", "Alexander", "vertex77");
//
//                Statement st = con.createStatement();
//
//                ResultSet rs = st.executeQuery("select * from place ");
//
//                data.clear();
//                int acceptedOrderCount = 0;
//                while (rs.next()) {
//                    Map<String, Object> item = new HashMap<>();
//
//                    String tableId = rs.getString(1);
//                    item.put(TAG_ID, tableId);
//                    item.put(TAG_NAME, rs.getString(2));
//                    item.put(TAG_MENU_TABLE, rs.getString(3));
//                    item.put(TAG_MENU_SEAT, rs.getString(4));
//
//                    BarOrder barOrder = findOrder(tableId);
//                    if (barOrder == null) {
//                        item.put(TAG_ORDER_NUMBER, "");
//                        item.put(TAG_ORDER_MAN, "");
//                        data.add(item);
//                    } else {
//                        item.put(TAG_ORDER_NUMBER, barOrder.getNumber());
//                        item.put(TAG_ORDER_MAN, barOrder.getManName());
//                        data.add(acceptedOrderCount++, item);
//                    }
//
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            LoadListOrder();
//            LoadPlace();
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            simpleAdapter.notifyDataSetChanged();
//        }
//    }

}
