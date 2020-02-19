package com.example.myapplication4;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    String[] name = {"Иван", "Марья", "Петр", "Антон", "Даша", "Борис",
            "Костя", "Игорь"};
    String[] position = {"Программер", "Бухгалтер", "Программер",
            "Программер", "Бухгалтер", "Директор", "Программер", "Охранник"};
    int salary[] = {13000, 10000, 13000, 13000, 10000, 15000, 13000, 8000};

    int[] colors = new int[2];

    TextView tv;
    MyTask mt;
    LinearLayout linLayout;
    LayoutInflater ltInflater;
    View.OnClickListener oclBtnOk;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colors[0] = Color.parseColor("#559966CC");
        colors[1] = Color.parseColor("#55336699");

        linLayout = (LinearLayout) findViewById(R.id.linLayout);

        ltInflater = getLayoutInflater();

        oclBtnOk = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                TextView tvName = v.findViewById(R.id.tvName);
                Log.i("myLog", tvName.getText().toString());
            }
        };

        fill(ltInflater, oclBtnOk);

        tv = (TextView) this.findViewById(R.id.tv_data);

        mt = new MyTask();
        mt.execute();
    }

    private void fill(LayoutInflater ltInflater, View.OnClickListener oclBtnOk) {
        for (int i = 0; i < name.length; i++) {
            View item = ltInflater.inflate(R.layout.item, linLayout, false);

            item.setOnClickListener(oclBtnOk);

            TextView tvName = (TextView) item.findViewById(R.id.tvName);
            tvName.setText(name[i]);
            TextView tvPosition = (TextView) item.findViewById(R.id.tvPosition);
            tvPosition.setText("Должность: " + position[i]);
            TextView tvSalary = (TextView) item.findViewById(R.id.tvSalary);
            tvSalary.setText("Оклад: " + String.valueOf(salary[i]));
            item.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            item.setBackgroundColor(colors[i % 2]);
            linLayout.addView(item);
        }
    }

    private void fill(Iterator<String> names) {
        int i = 0;
        while (names.hasNext()) {
            String key = names.next();

            View item = ltInflater.inflate(R.layout.item, linLayout, false);

            item.setOnClickListener(oclBtnOk);

            TextView tvName = (TextView) item.findViewById(R.id.tvName);
            tvName.setText(key);
            TextView tvPosition = (TextView) item.findViewById(R.id.tvPosition);
            //tvPosition.setText("Должность: " + position[i]);
            TextView tvSalary = (TextView) item.findViewById(R.id.tvSalary);
            //tvSalary.setText("Оклад: " + String.valueOf(salary[i]));
            item.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            item.setBackgroundColor(colors[i++ % 2]);
            linLayout.addView(item);
        }
    }

    class MyTask extends AsyncTask<Void, Void, Void> {
        Iterator<String> level;
        public void testDB() {
            try {

                Class.forName("com.mysql.jdbc.Driver");

                // perfect

                // localhost

                /*
                 * Connection con = DriverManager .getConnection(
                 * "jdbc:mysql://192.168.1.5:3306/databasename?user=root&password=123"
                 * );
                 */

                // online testing
                Connection con = DriverManager.getConnection("jdbc:mysql://178.46.165.64:3306/bar", "Alexander", "vertex77");

                String result = "Database connection success\n";
                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("select * from menu ");
                ResultSetMetaData rsmd = rs.getMetaData();

                while (rs.next()) {

                    result += rsmd.getColumnName(1) + ": " + rs.getString(1) + "\n";
                    result += rsmd.getColumnName(2) + ": " + rs.getString(2) + "\n";
                    BarMenu barMenu = new BarMenu(rs.getString(2));

                    level = barMenu.firstLevel();
                }
                Log.d("myLogs", result);
            } catch (Exception e) {
                e.printStackTrace();
                tv.setText(e.toString());
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tv.setText("Begin");
        }

        @Override
        protected Void doInBackground(Void... params) {
            testDB();
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            tv.setText("End");
            fill(level);
        }
    }
}
