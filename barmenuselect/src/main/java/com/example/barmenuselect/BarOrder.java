package com.example.barmenuselect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BarOrder {
    private ArrayList<BarOrderItem> items;

    final static String TAG_MENU_COLOR = "color";
    final static String TAG_MENU_ID = "id";
    final static String TAG_MENU_NAME = "name";
    final static String TAG_MENU_UNIT = "unit";
    final static String TAG_MENU_PRICE = "price";
    final static String TAG_ORDER_COUNT = "count";
    final static String TAG_MENU_GROUP = "group";


    public BarOrder() {
        super();
        items = new ArrayList<>();
    }

    public BarOrder(String barOrderJSON) {
        super();
        items = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(barOrderJSON);
            JSONArray names = obj.names();
            for (int i = 0; names != null && i < names.length(); ++i) {
                String id = names.getString(i);

                JSONObject item = obj.optJSONObject(id);
                items.add(new BarOrderItem(id, item.optInt("Количество"), item));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int getIndex(String _id) {
        return items.indexOf(new BarOrderItem(_id, 0, null));
    }

    public void add(String _id, JSONObject menuItem) {
        int index = getIndex(_id);
        if (index < 0) {
            items.add(new BarOrderItem(_id, 1, menuItem));
        } else {
            items.get(index).inc();
        }
    }

    public void remove(String _id) {
        int index = getIndex(_id);
        if (index >= 0) {
            items.get(index).dec();
            if (items.get(index).getCount() == 0) {
                items.remove(index);
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject obj = getJsonObject();

        return obj.toString();
    }

    private JSONObject getJsonObject() {
        JSONObject obj;

        obj = new JSONObject();
        for (int i = 0; i < items.size(); ++i) {
            try {
                JSONObject subObj = items.get(i).getObject();
                subObj.put("Количество", items.get(i).getCount());
                obj.put(items.get(i).getId(), subObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    public int getCount(String _id) {
        int index = getIndex(_id);
        if (index >= 0) {
            return items.get(index).getCount();
        }
        return 0;
    }

    ArrayList<Map<String, Object>> getArrayList() {

        ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        JSONObject jCurObject = getJsonObject();
        JSONArray names = jCurObject.names();
        for (int i = 0; names != null && i < names.length(); ++i) {
            Map<String, Object> item = new HashMap<String, Object>();

            try {
                String namesString = names.getString(i);

                if (null == jCurObject.optJSONObject(namesString)) {
                    continue;
                }

                int isGroup = jCurObject.getJSONObject(namesString).optInt("Группа");
                String colorMenu = jCurObject.getJSONObject(namesString).optString("Цвет");
                if (isGroup == 1 && colorMenu.isEmpty()) {
                    colorMenu = "#E3F2FD";
                }
                item.put(TAG_MENU_ID, namesString);
                item.put(TAG_MENU_COLOR, colorMenu);
                item.put(TAG_MENU_NAME, jCurObject.getJSONObject(namesString).optString("Наименование"));
                item.put(TAG_MENU_UNIT, jCurObject.getJSONObject(namesString).optString("Единица"));
                item.put(TAG_MENU_PRICE, jCurObject.getJSONObject(namesString).optString("Цена"));
                item.put(TAG_ORDER_COUNT, jCurObject.getJSONObject(namesString).optInt("Количество"));
                item.put(TAG_MENU_GROUP, isGroup);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            result.add(item);
        }

        return result;
    }


    private class BarOrderItem {
        private String id;
        private int count;
        private JSONObject menuItem;

        @Override
        public boolean equals(@Nullable Object obj) {
            BarOrderItem item = (BarOrderItem) obj;

            return item.id.equals(this.id);
        }

        public BarOrderItem(String _id, int _count, JSONObject _menuItem) {
            super();
            id = _id;
            count = _count;
            menuItem = _menuItem;
        }

        public void inc() {
            ++count;
        }

        public void dec() {
            --count;
        }

        public int getCount() {
            return count;
        }

        public String getId() {
            return id;
        }

        public JSONObject getObject() {
            return menuItem;
        }

    }
}
