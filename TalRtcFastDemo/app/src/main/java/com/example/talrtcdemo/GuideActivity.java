package com.example.talrtcdemo;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GuideActivity extends Activity {

    private PermissionUtils permissionUtils;
    private Button mJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        mJoin = findViewById(R.id.bt_join);
        mJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(GuideActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        permissionUtils = new PermissionUtils();
        if (!permissionUtils.hasPermissions(GuideActivity.this)) {

            permissionUtils.requestPermissions(GuideActivity.this, new PermissionUtils.Callback() {
                @Override
                public void onSuccess() {
//                    havePermission = true;
                }

                @Override
                public void onFailure(String msg) {

                }
            });
        } else {
//            havePermission = true;
        }
    }
}