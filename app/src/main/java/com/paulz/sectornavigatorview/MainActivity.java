package com.paulz.sectornavigatorview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    NavigationBar mNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        int color1 = getResources().getColor(android.R.color.holo_blue_bright);
        int color2 = getResources().getColor(android.R.color.holo_blue_dark);
        mNavigationBar = (NavigationBar) findViewById(R.id.layout_navigation);
        try {
            mNavigationBar.setComponents(4, new String[]{"tab1", "tab2", "tab3", "tab4"}, new int[]{color1, color1, color1, color1}, new int[]{color2, color2, color2, color2});
        } catch (Exception e) {
            e.printStackTrace();
        }
        mNavigationBar.setChecked(0);

        mNavigationBar.setOnCheckedChangeListener(new NavigationBar.OnCheckedChangeListener() {
            @Override
            public void onCheckedChange(int position, int oldPosition,boolean changeBySelf) {
                Toast.makeText(getApplicationContext(),"从区域"+oldPosition+"切换到了区域"+position,Toast.LENGTH_LONG).show();
            }
        });
    }

}
