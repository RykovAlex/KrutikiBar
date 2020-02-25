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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import static com.example.barmenuselect.BarOrder.TAG_MENU_COLOR;
import static com.example.barmenuselect.BarOrder.TAG_MENU_GROUP;
import static com.example.barmenuselect.BarOrder.TAG_MENU_ID;
import static com.example.barmenuselect.BarOrder.TAG_MENU_NAME;
import static com.example.barmenuselect.BarOrder.TAG_MENU_PRICE;
import static com.example.barmenuselect.BarOrder.TAG_MENU_UNIT;
import static com.example.barmenuselect.BarOrder.TAG_ORDER_COUNT;

public class BarOrderActivity extends AppCompatActivity implements View.OnClickListener, SimpleAdapter.ViewBinder, AdapterView.OnItemClickListener {
    private BarOrder barOrder;
    private ListView lvOrder;
    private ArrayList<Map<String, Object>> data;
    private SimpleAdapter simpleAdapter;
    private int slot = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_order);

        barOrder = new BarOrder();
        lvOrder = findViewById(R.id.lvOrder);

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {TAG_MENU_ID, TAG_MENU_NAME, TAG_MENU_UNIT, TAG_MENU_PRICE, TAG_ORDER_COUNT, TAG_ORDER_COUNT, TAG_MENU_COLOR, TAG_MENU_GROUP};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = {R.id.tvId, R.id.tvName, R.id.tvUnit, R.id.tvPrice, R.id.llCount, R.id.tvCount, R.id.llMain, R.id.llName};

        data = new ArrayList<Map<String, Object>>();
        simpleAdapter = new SimpleAdapter(this, data, R.layout.menu_item, from, to);
        simpleAdapter.setViewBinder(this);

        lvOrder.setAdapter(simpleAdapter);
        lvOrder.setOnItemClickListener(this);

        refreshList();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            barOrder = new BarOrder(data.getStringExtra("barOrder"));
            refreshList();
        }
    }

    private void refreshList() {
        data.clear();
        data.addAll(barOrder.getArrayList());
        simpleAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.bnMenu:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("barOrder", barOrder.toString());
                startActivityForResult(intent, 0);
                break;
            case R.id.bnSave:
                SaveOrderTask saveOrderTask = new SaveOrderTask();
                saveOrderTask.execute();
                break;
            case R.id.bnOrder:
                break;
            case R.id.ibDown:
                String index = getIndex(v);
                barOrder.remove(index);
                refreshList();
                break;
            case R.id.ibUp:
                index = getIndex(v);
                barOrder.add(index, null);
                refreshList();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }

    private String getIndex(View v) {
        View rootLayout = (View) v.getParent().getParent();
        View layout = rootLayout.findViewById(R.id.llName);

        return ((TextView) layout.findViewById(R.id.tvId)).getText().toString();
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
            case R.id.llName:
                int isGroup = (int) data;
                if (isGroup == 1) {
                    //((TextView)view.findViewById(R.id.tvName)).setTextSize(24);
                } else {
                    //((TextView)view.findViewById(R.id.tvName)).setTextSize(14);
                }
                return true;
            case R.id.llMain:
                String color = (String) data;
                if (color.isEmpty()) {
                    view.setBackgroundColor(Color.WHITE);
                } else {
                    view.setBackgroundColor(Color.parseColor(color));
                }
                return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    class SaveOrderTask extends AsyncTask<Void, Void, Void> {

        private void SaveOrder() {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://178.46.165.64:3306/bar", "Alexander", "vertex77");

                Statement st = con.createStatement();
                String strSlot = String.format("%d", slot);
                if ( 0 == st.executeUpdate("update orders set orders.order='" +barOrder.toString()+ "' where id = " + strSlot)){
                    st.executeUpdate("insert into orders (id, orders.order) values (" +strSlot+",'" +barOrder.toString()+ "')");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SaveOrder();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
