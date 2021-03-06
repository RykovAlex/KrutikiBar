package ru.krutiki.barkrutikihotel;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.barkrutikihotel.R;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class BarOrderActivity extends AppCompatActivity implements View.OnClickListener, SimpleAdapter.ViewBinder {
    private BarOrder barOrder;
    private ArrayList<Map<String, Object>> data;
    private SimpleAdapter simpleAdapter;
    private View infoHeader;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_order);
        context = this;

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        Intent intent = getIntent();
        String jsonOrder = intent.getStringExtra(BarOrder.TAG_INTENT_ORDER);

        barOrder = new BarOrder(jsonOrder);
        ListView lvOrder = findViewById(R.id.lvOrder);

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {BarOrder.TAG_ID, BarOrder.TAG_NAME, BarOrder.TAG_MENU_UNIT, BarOrder.TAG_MENU_PRICE, BarOrder.TAG_ORDER_COUNT, BarOrder.TAG_JSON_PRINT_COUNT, BarOrder.TAG_MENU_COLOR, BarOrder.TAG_MENU_GROUP, BarOrder.TAG_JSON_COUNT, BarOrder.TAG_JSON_DELETED_COUNT};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = {R.id.tvId, R.id.tvName, R.id.tvUnit, R.id.tvPrice, R.id.llCount, R.id.tvPrintCount, R.id.llMain, R.id.llName, R.id.tvCount, R.id.tvDeletedCount};

        data = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(this, data, R.layout.menu_item, from, to);
        simpleAdapter.setViewBinder(this);

        lvOrder.setAdapter(simpleAdapter);

        infoHeader = createInfoHeader(lvOrder);
        lvOrder.addHeaderView(infoHeader);
        refreshList();
    }

    private View createInfoHeader(ListView parent) {
        View v = getLayoutInflater().inflate(R.layout.info_header, parent, false);
        v.setBackgroundColor(Color.parseColor("#DCEDC8"));
        ((TextView) v.findViewById(R.id.tvName)).setText(barOrder.getManName());
        ((TextView) v.findViewById(R.id.tvTable)).setText(barOrder.getTableName());
        return v;
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

        String formattedDouble = String.format(Locale.US, "%.02f", barOrder.getPrice());
        ((TextView) infoHeader.findViewById(R.id.tvSum)).setText(formattedDouble);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bnMenu:
                Intent intent = new Intent(this, BarMenuActivity.class);
                intent.putExtra("barOrder", barOrder.toString());
                startActivityForResult(intent, 0);
                break;
            case R.id.bnSave:
                SaveOrderTask saveOrderTask = new SaveOrderTask();
                barOrder.setToKitchen(false);
                saveOrderTask.execute(barOrder.getTableId(), barOrder.toString());

                break;
            case R.id.bnOrder:
                saveOrderTask = new SaveOrderTask();
                barOrder.setToKitchen(true);
                saveOrderTask.execute(barOrder.getTableId(), barOrder.toString());

                break;
            case R.id.ibDown:
                String index = getIndex(v);
                barOrder.sub(index);
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
                double count = (double) data;
//                if (count > 0) {
//                    view.setVisibility(View.VISIBLE);
//                } else {
//                    view.setVisibility(View.GONE);
//                }
                view.setVisibility(View.VISIBLE);
                return true;
            case R.id.llName:
//                int isGroup = (int) data;
//                if (isGroup == 1) {
//                    //((TextView)view.findViewById(R.id.tvName)).setTextSize(24);
//                } else {
//                    //((TextView)view.findViewById(R.id.tvName)).setTextSize(14);
//                }
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

    @SuppressLint("StaticFieldLeak")
    class SaveOrderTask extends AsyncTask<String, Void, Void> {

        private void SaveOrder(String id, String order) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                DriverManager.setLoginTimeout(5);
                Connection con = DriverManager.getConnection(BarOrder.getChannelIp(), getString(R.string.user_name), getString(R.string.user_password));

                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("select * from order_list order by id desc limit 1");
                String lastTimestamp;
                if (rs.next()) {
                    lastTimestamp = rs.getString(1);
                } else {
                    lastTimestamp = "empty";
                }

                if (0 == st.executeUpdate("update orders set orders.order='" + order + "' where id = '" + id + "'")) {
                    st.executeUpdate("insert into orders (id, orders.order) values ('" + id + "','" + order + "')");
                }

                for (int i = 0; i < 20; ++i) {
                    rs = st.executeQuery("select * from order_list order by id desc limit 1");
                    if (rs.next()) {
                        if (!lastTimestamp.equals(rs.getString(1))) {
                            break;
                        }
                    }
                    Thread.sleep(1000);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Runs on the UI thread before {@link #doInBackground}.
         *
         * @see #onPostExecute
         * @see #doInBackground
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDialog pd = new ProgressDialog(context);
            pd.setTitle("Обработка заказа");
            pd.setMessage("Идет процесс проведения заказа официанта в 1С:Трактир. Ждите ...");
            // добавляем кнопку
            pd.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            pd.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            SaveOrder(params[0], params[1].replace("\"", "\\\""));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
        }
    }

}
