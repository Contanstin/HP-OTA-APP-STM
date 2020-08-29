package com.hpmont.hpprog.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import com.hpmont.hpprog.R;
import com.hpmont.hpprog.util.CommonUtil;

public class BaseActivity extends Activity {
    protected MyApplication application;
//    protected ChipType chipType;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
    }

    /**
     * 连续点击两次（2秒内）返回键强制退出程序
     */
    private long extiTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - extiTime) > 2000) {
                CommonUtil.toast(getResources().getString(R.string.exit));
                extiTime = System.currentTimeMillis();
            }
            else {
                finish();
                application.exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }
}
