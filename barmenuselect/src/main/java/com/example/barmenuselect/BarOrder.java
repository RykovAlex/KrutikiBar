package com.example.barmenuselect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class BarOrder {
    private ArrayList<BarOrderItem> items;

    public BarOrder() {
        super();
        items = new ArrayList<>();
    }

    private int getIndex(String _id) {
        return items.indexOf(new BarOrderItem(_id, 0));
    }

    public void add(String _id) {
        int index = getIndex(_id);
        if (index < 0) {
            items.add(new BarOrderItem(_id, 1));
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

    public int getCount(String _id) {
        int index = getIndex(_id);
        if (index >= 0) {
            return items.get(index).getCount();
        }
        return 0;
    }

    private class BarOrderItem {
        private String id;
        private int count;

        @Override
        public boolean equals(@Nullable Object obj) {
            BarOrderItem item = (BarOrderItem)obj;

            return item.id.equals(this.id);
        }

        public BarOrderItem(String _id, int _count) {
            super();
            id = _id;
            count = _count;
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
    }
}
