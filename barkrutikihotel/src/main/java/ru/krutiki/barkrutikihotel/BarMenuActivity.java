package ru.krutiki.barkrutikihotel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.barkrutikihotel.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class BarMenuActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, SimpleAdapter.ViewBinder, View.OnClickListener {
    BarMenu barMenu;
    ListView lvMenu;
    ArrayList<Map<String, Object>> data;
    SimpleAdapter simpleAdapter;
    BarOrder barOrder;

    @Override
    public void onBackPressed() {
        ok();
        super.onBackPressed();
    }

    final String TAG_MENU_COLOR_BACKGROUND = "colorBackground";
    final String TAG_MENU_COLOR_TEXT = "colorText";
    final String TAG_MENU_ID = "id";
    final String TAG_MENU_NAME = "name";
    final String TAG_MENU_UNIT = "unit";
    final String TAG_MENU_PRICE = "price";
    final String TAG_ORDER_COUNT = "count";
    final String TAG_MENU_GROUP = "group";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvMenu = findViewById(R.id.lvMenu);

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = {TAG_MENU_ID, TAG_MENU_NAME, TAG_MENU_UNIT, TAG_MENU_PRICE, TAG_ORDER_COUNT, TAG_ORDER_COUNT, TAG_MENU_COLOR_BACKGROUND, TAG_MENU_COLOR_TEXT, TAG_MENU_GROUP};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = {R.id.tvId, R.id.tvName, R.id.tvUnit, R.id.tvPrice, R.id.llCount, R.id.tvPrintCount, R.id.llMain, R.id.tvName, R.id.llName};

        data = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(this, data, R.layout.menu_item, from, to);
        simpleAdapter.setViewBinder(this);

        lvMenu.setAdapter(simpleAdapter);
        lvMenu.setOnItemClickListener(this);

        Intent intent = getIntent();
        barOrder = new BarOrder(intent.getStringExtra("barOrder"));

        LoadMenuTask loadMenuTask = new LoadMenuTask();
        loadMenuTask.execute();
    }

    // создание Header
    View createHeader(String name, String id, String color, ListView lvMenu) {
        View v = getLayoutInflater().inflate(R.layout.menu_header, lvMenu, false);
        if (color.isEmpty()) {
            v.setBackgroundColor(Color.WHITE);
        } else {
            try {
                //v.setBackgroundColor(Color.parseColor(color));
                int[] colors = {Color.parseColor("#FFFFFF"), Color.parseColor(color)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                gd.setCornerRadius(0f);
                v.setBackgroundDrawable(gd);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

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
                barOrder.add(index, barMenu.getMenuItem(index));
                index = null;
            }
            refreshList(index);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation) {
        switch (view.getId()) {
            case R.id.tvName:
                String colorText = (String) data;
                if (!colorText.isEmpty() && colorText.charAt(0) == '#') {
                    ((TextView) view).setTextColor(Color.parseColor(colorText));
                    return true;
                }
                return false;
            case R.id.llCount:
                double count = (double) data;
                if (count > 0) {
                    view.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.tvCount).setVisibility(View.GONE);
                    view.findViewById(R.id.tvDeletedCount).setVisibility(View.GONE);
                } else {
                    view.setVisibility(View.GONE);
                }
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
                    //view.setBackgroundColor(Color.parseColor(color));
                    try {
                        //int[] colors = {Color.parseColor("#FFFFFF"),Color.parseColor("#FFFFFF"), Color.parseColor(color)};
                        int[] colors = {Color.WHITE, Color.parseColor(color)};
                        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                        gd.setCornerRadius(0f);
                        //gd.setGradientCenter((float)0.05, (float)0.05);
                        //gd.setStroke(8, Color.WHITE);
                        view.setBackgroundDrawable(gd);

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
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
                barOrder.add(index, barMenu.getMenuItem(index));
                break;
        }
        refreshList(null);
    }

    private void ok() {
        Intent intent = new Intent();
        intent.putExtra("barOrder", barOrder.toString());
        setResult(RESULT_OK, intent);
    }

    private void refreshList(String index) {
        data.clear();
        data.addAll(barMenu.getLevel(index));
        simpleAdapter.notifyDataSetChanged();
    }

    @SuppressLint("StaticFieldLeak")
    class LoadMenuTask extends AsyncTask<Void, Void, Void> {

        private void LoadMenu() {
            for (int i = 0; i < 2; ++i) {
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    DriverManager.setLoginTimeout(5);
                    Connection con = DriverManager.getConnection(BarOrder.getChannelIp(), getString(R.string.user_name), getString(R.string.user_password));

                    Statement st = con.createStatement();

                    ResultSet rs = st.executeQuery("select * from menu ");

                    while (rs.next()) {
                        barMenu = new BarMenu(rs.getString(2));
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
            LoadMenu();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            data.clear();
            data.addAll(barMenu.getLevel(null));
            simpleAdapter.notifyDataSetChanged();
            if (getSupportActionBar() != null)
                getSupportActionBar().setSubtitle(BarOrder.getChannelName());
        }
    }

    private class BarMenu {
        private JSONObject jCurObject;
        private ArrayList<String> selectedKeys;
        private HashMap<String, JSONObject> maps;
        private HashMap<String, View> headers;

        BarMenu(String jsonStr) {
            super();
            try {
                jCurObject = new JSONObject(jsonStr);

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

            ArrayList<Map<String, Object>> result = new ArrayList<>();

            if (index == null) {
                if (jCurObject != null) {
                    JSONArray names = jCurObject.names();
                    for (int i = 0; names != null && i < names.length(); ++i) {
                        Map<String, Object> item = new HashMap<>();

                        try {
                            String namesString = names.getString(i);

                            if (null == jCurObject.optJSONObject(namesString)) {
                                continue;
                            }

                            int isGroup = jCurObject.getJSONObject(namesString).optInt("Группа");
                            String colorBackground = jCurObject.getJSONObject(namesString).optString("ЦветФона");
                            if (isGroup == 1 && colorBackground.isEmpty()) {
                                colorBackground = "#E3F2FD";
                            }
                            String colorText = jCurObject.getJSONObject(namesString).optString("ЦветТекста");
                            if (colorText.isEmpty()) {
                                colorText = "#000000";
                            }
                            item.put(TAG_MENU_ID, namesString);
                            item.put(TAG_MENU_COLOR_BACKGROUND, colorBackground);
                            item.put(TAG_MENU_COLOR_TEXT, colorText);
                            item.put(TAG_MENU_NAME, jCurObject.getJSONObject(namesString).optString("Наименование"));
                            item.put(TAG_MENU_UNIT, jCurObject.getJSONObject(namesString).optString("Единица"));
                            item.put(TAG_MENU_PRICE, jCurObject.getJSONObject(namesString).optString("Цена"));
                            item.put(TAG_ORDER_COUNT, barOrder.getPrintCount(namesString));
                            item.put(TAG_MENU_GROUP, isGroup);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        result.add(item);
                    }

                    // Sorting
                    Collections.sort(result, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                            String s1 = (String) o1.get(TAG_MENU_NAME);
                            String s2 = (String) o2.get(TAG_MENU_NAME);
                            int group1 = (int) o1.get(TAG_MENU_GROUP);
                            int group2 = (int) o2.get(TAG_MENU_GROUP);
                            if (s1 == null && s2 == null) {
                                return 0;
                            }
                            if (s1 == null) {
                                return 1;
                            }
                            if (s2 == null) {
                                return -1;
                            }
                            if (group1 == 0 && group2 == 0) {
                                return s1.compareTo(s2);
                            }
                            if (group1 == 0 && group2 > 0) {
                                return 1;
                            }
                            if (group1 > 0 && group2 == 0) {
                                return -1;
                            }
                            return s1.compareTo(s2);
                        }
                    });

                }
            } else {
                try {
                    View view;

                    view = createHeader(jCurObject.getJSONObject(index).optString("Наименование"), index, jCurObject.getJSONObject(index).optString("ЦветФона"), lvMenu);
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

        JSONObject getMenuItem(String index) {
            return jCurObject.optJSONObject(index);
        }
    }
}
