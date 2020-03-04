package ru.krutiki.barkrutikihotel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BarOrder {

    private String number;
    private ArrayList<BarOrderItem> items;

    final static String TAG_MENU_COLOR = "color";
    final static String TAG_ID = "id";
    final static String TAG_NAME = "name";
    final static String TAG_MENU_UNIT = "unit";
    final static String TAG_MENU_PRICE = "price";
    final static String TAG_ORDER_COUNT = "count";
    final static String TAG_MENU_GROUP = "group";
    final static String TAG_MENU_PERMISSION = "permission";
    final static String TAG_JSON_MAN_ID = "Сотрудник";
    final static String TAG_JSON_MAN_NAME = "СотрудникНаименование";
    final static String TAG_JSON_TABLE_ID = "Столик";
    final static String TAG_JSON_TABLE_NAME = "СтоликНаименование";
    final static String TAG_JSON_COUNT = "Количество";
    final static String TAG_JSON_PRINT_COUNT = "Печатать";
    final static String TAG_JSON_DELETED_COUNT = "Удалено";
    private final static String TAG_JSON_ORDER_NUMBER = "НомерДок";
    final static String TAG_INTENT_ORDER = "barOrder";
    final static String TAG_JSON_ORDER_AMOUNT = "Сумма";

    private String manId;
    private String manName;
    private String tableId;
    private String tableName;
    private boolean toKitchen;
    private double amount;

    private static final String[] channelIpList = {"jdbc:mysql://192.168.0.1:3306/bar","jdbc:mysql://178.46.165.64:3306/bar"};
    private static String channelIp = channelIpList[0];

    static String getChannelIp(){
        return channelIp;
    }

    static void switchChannelIp(){
        if (channelIp.equals(channelIpList[0])) {
            channelIp = channelIpList[1];
        } else {
            channelIp = channelIpList[0];
        }
    }

    BarOrder(String barOrderJSON) {
        super();
        try {
            parse(new JSONObject(barOrderJSON));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    BarOrder(String _number, JSONObject jsonObject) {
        super();
        parse(jsonObject);
        number = _number;
    }

    private void parse(JSONObject jsonObject) {
        manId = jsonObject.optString(TAG_JSON_MAN_ID);
        manName = jsonObject.optString(TAG_JSON_MAN_NAME);
        tableId = jsonObject.optString(TAG_JSON_TABLE_ID);
        tableName = jsonObject.optString(TAG_JSON_TABLE_NAME);
        number = jsonObject.optString(TAG_JSON_ORDER_NUMBER);
        amount = jsonObject.optDouble(TAG_JSON_ORDER_AMOUNT);

        items = new ArrayList<>();
        JSONArray names = jsonObject.names();
        try {
            for (int i = 0; names != null && i < names.length(); ++i) {
                String id = names.getString(i);
                JSONObject item = jsonObject.optJSONObject(id);
                if (item != null) {
                    items.add(new BarOrderItem(id, item.optDouble(TAG_JSON_COUNT), item.optDouble(TAG_JSON_PRINT_COUNT), item.optDouble(TAG_JSON_DELETED_COUNT), item));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int getIndex(String _id) {
        return items.indexOf(new BarOrderItem(_id, 0, 0, 0, null));
    }

    void add(String _id, JSONObject menuItem) {
        int index = getIndex(_id);
        if (index < 0) {
            items.add(new BarOrderItem(_id, 0, 1, 0, menuItem));
        } else {
            items.get(index).inc();
        }
    }

    void remove(String _id) {
        int index = getIndex(_id);
        if (index >= 0) {
            items.get(index).dec();
            if (items.get(index).getTotalCount() == 0) {
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
        try {
            if (!number.isEmpty()) {
                obj.put(TAG_JSON_ORDER_NUMBER, number);
            }
            obj.put("Заказ", toKitchen ? "да" : "нет");
            obj.put(TAG_JSON_MAN_ID, manId);
            obj.put(TAG_JSON_MAN_NAME, manName);
            obj.put(TAG_JSON_TABLE_ID, tableId);
            obj.put(TAG_JSON_TABLE_NAME, tableName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < items.size(); ++i) {
            try {
                JSONObject subObj = items.get(i).getObject();
                subObj.put(TAG_JSON_COUNT, items.get(i).getCount());
                subObj.put(TAG_JSON_PRINT_COUNT, items.get(i).getPrintCount());
                subObj.put(TAG_JSON_DELETED_COUNT, items.get(i).getDeletedCount());
                obj.put(items.get(i).getId(), subObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    double getPrice() {
        double result = 0;
        for (int i = 0; i < items.size(); ++i) {
            result += items.get(i).getPrice();
        }
        return result;
    }

    double getPrintCount(String _id) {
        int index = getIndex(_id);
        if (index >= 0) {
            return items.get(index).getPrintCount();
        }
        return 0;
    }

    ArrayList<Map<String, Object>> getArrayList() {

        ArrayList<Map<String, Object>> result = new ArrayList<>();
        JSONObject jCurObject = getJsonObject();
        JSONArray names = jCurObject.names();
        for (int i = 0; names != null && i < names.length(); ++i) {
            Map<String, Object> item = new HashMap<>();

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
                item.put(TAG_ID, namesString);
                item.put(TAG_MENU_COLOR, colorMenu);
                item.put(TAG_NAME, jCurObject.getJSONObject(namesString).optString("Наименование"));
                item.put(TAG_MENU_UNIT, jCurObject.getJSONObject(namesString).optString("Единица"));
                item.put(TAG_MENU_PRICE, jCurObject.getJSONObject(namesString).optString("Цена"));
                double printCount = jCurObject.getJSONObject(namesString).optDouble(TAG_JSON_PRINT_COUNT);
                item.put(TAG_JSON_PRINT_COUNT, printCount);
                double count = jCurObject.getJSONObject(namesString).optDouble(TAG_JSON_COUNT);
                item.put(TAG_JSON_COUNT, count);
                double deletedCount = jCurObject.getJSONObject(namesString).optDouble(TAG_JSON_DELETED_COUNT);
                item.put(TAG_JSON_DELETED_COUNT, deletedCount);
                item.put(TAG_ORDER_COUNT, printCount + count + deletedCount);

                item.put(TAG_MENU_GROUP, isGroup);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            result.add(item);
        }

        return result;
    }

    String getTableId() {
        return tableId;
    }

    String getNumber() {
        return number;
    }

    String getManName() {
        return manName;
    }

    String getTableName() {
        return tableName;
    }

    void setToKitchen(boolean b) {
        toKitchen = b;
    }

    double getAmount() {
        return amount;
    }


    private static class BarOrderItem {
        private String id;
        private double count;
        private double printCount;
        private double deletedCount;
        private JSONObject menuItem;

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;

            BarOrderItem other = (BarOrderItem) obj;

            return other.id.equals(this.id);
        }

        BarOrderItem(String _id, double _count, double _printCount, double _deletedCount, JSONObject _menuItem) {
            super();
            id = _id;
            count = _count;
            printCount = _printCount;
            deletedCount = _deletedCount;
            menuItem = _menuItem;
        }

        void inc() {
            ++printCount;
        }

        void dec() {
            if (printCount > 0)
                --printCount;
        }

        double getPrintCount() {
            return printCount;
        }

        public String getId() {
            return id;
        }

        JSONObject getObject() {
            return menuItem;
        }

        double getDeletedCount() {
            return deletedCount;
        }

        double getCount() {
            return count;
        }

        double getTotalCount() {
            return count + printCount - deletedCount;
        }

        double getPrice() {
            double price = Double.parseDouble(menuItem.optString("Цена").replace(",", ""));
            return getTotalCount() * price;
        }

    }
}

