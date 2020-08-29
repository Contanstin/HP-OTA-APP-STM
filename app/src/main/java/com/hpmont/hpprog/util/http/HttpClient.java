package com.hpmont.hpprog.util.http;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.common.primitives.Bytes;
import com.hpmont.hpprog.R;
import com.hpmont.hpprog.config.Config;
import com.hpmont.hpprog.util.CommonUtil;
import com.hpmont.hpprog.util.EncryptHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import org.apache.http.Header;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

public class HttpClient {
    private static AsyncHttpClient client = new AsyncHttpClient();
    private static SyncHttpClient client_syn = new SyncHttpClient();

    /**
     * GET方法网络请求
     * @param mContext
     * @param url 接口地址
     * @param params 参数
     * @param msg
     * @param callBack 回调
     */
    public static void get(final Context mContext, String url, RequestParams params, String msg, final httpCallBack callBack) {
        //网络状态
        if (!NetWorkState.netWorkState(mContext)) {
            return;
        }
        final ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setCancelable(true);
        // 设置进度条的形式为圆形转动的进度条
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置在点击Dialog外是否取消Dialog进度条
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(msg);
        if (!msg.equals("")) {
            dialog.show();
        }

        client.setTimeout(10000);
        client.get(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                callBack.success(new String(arg2));
            }
            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                callBack.fail(ConnectFailMessage.connetFailMessage(mContext,arg3.toString()));
            }
            @Override
            public void onFinish() {
                super.onFinish();
                dialog.dismiss();
            }
        });
    }

    /**
     * POST方法网络请求
     * @param mContext
     * @param url 接口地址
     * @param params 参数
     * @param msg 描述
     * @param callBack 回调
     */
    public static void post(final Context mContext, String url, RequestParams params, String msg, final httpCallBack callBack) {
        //网络状态
        if (!NetWorkState.netWorkState(mContext)) {
            CommonUtil.toast(mContext.getString(R.string.check_network));
            return;
        }

        final AlertDialog builder = new AlertDialog.Builder(mContext).create();
        View view = LayoutInflater.from(mContext).inflate(R.layout.bar_progress, null);
        TextView txt_progress = (TextView) view.findViewById(R.id.progressText);
        builder.setCancelable(true);
        builder.setCanceledOnTouchOutside(false);
        txt_progress.setText(msg);
        if (!msg.equals("")) {
            Activity a = (Activity) mContext;
            if (!a.isFinishing()) {
                builder.show();
                builder.getWindow().setContentView(view);
            }
        }

        client.setTimeout(10000);
        client.addHeader("Expect","100-continue");
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                callBack.success(new String(arg2));
            }
            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                callBack.fail(ConnectFailMessage.connetFailMessage(mContext,arg3.toString()));
            }
            @Override
            public void onFinish() {
                super.onFinish();
                builder.dismiss();
            }
        });
    }

    /**
     * POST方法网络请求（同步）
     * @param mContext
     * @param url 接口地址
     * @param params 参数
     * @param callBack 回调
     */
    public static void post_Syn(final Context mContext, String url, RequestParams params, final synHttpCallBack callBack){
        //网络状态
        if (!NetWorkState.netWorkState(mContext)) {
            CommonUtil.toast(mContext.getString(R.string.check_network));
            return;
        }
        client_syn.setTimeout(10000);
        client_syn.addHeader("Expect", "100-continue");
        client_syn.post(url, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callBack.success(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                callBack.fail(errorResponse);
            }
        });
    }

    /**
     * Boot文件下载（需线程中调用）
     * @param map
     * @param callBack
     */
    public static void downBootFile(Context mContext, Map map,final httpCallBack callBack){
        Map<String,String> downMap = map;
        int downCount = 0;
        //网络状态
        if (!NetWorkState.netWorkState(mContext)) {
            CommonUtil.toast(mContext.getString(R.string.check_network));
            return;
        }
        for(Map.Entry<String, String> entry : downMap.entrySet()){
            String url = entry.getKey();
            String fileName = entry.getValue();
            try{
                URL myurl = new URL(url);
                URLConnection conn = myurl.openConnection();
                InputStream inputStream = conn.getInputStream();
                int contentLength = conn.getContentLength();
                String path = Config.BOOT_PATH;
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdir();
                }
                byte[] bytes = new byte[1024];
                int len = 0;
                OutputStream outputStream = new FileOutputStream(path + fileName);
                while ((len = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, len);
                }
                downCount ++;
                outputStream.close();
                inputStream.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        if(downCount == downMap.size()){
            callBack.success("success");
        }
        else{
            callBack.fail("fail");
        }
    }

    /**
     * Hex文件下载
     * @param mContext
     * @param url
     * @return
     */
    public static String[] downHexFile(Context mContext, String url){
        byte[] bytes = null;
        //网络状态
        if (!NetWorkState.netWorkState(mContext)) {
            CommonUtil.toast(mContext.getString(R.string.check_network));
            return null;
        }
        try{
            URL myurl = new URL(url);
            URLConnection conn = myurl.openConnection();
            InputStream inputStream = conn.getInputStream();
            ArrayList<Byte> byteList = new ArrayList<>();
            int len = 0;
            byte[] tempBytes = new byte[1024];
            while ((len = inputStream.read(tempBytes, 0, 1024)) != -1){
                for(int i = 0; i < len; i ++){
                    byteList.add(tempBytes[i]);
                }
            }
            inputStream.close();
            bytes = Bytes.toArray(byteList);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if(bytes == null){
            return null;
        }
        byte[] hex = EncryptHelper.getBytes(bytes);
        String[] hexArry = new String(hex).replace("\r", "").split("\n");

        return  hexArry;
    }

    public interface httpCallBack {
        public void success(String res);
        public void fail(String error);
    }

    public interface synHttpCallBack {
        public void success(JSONObject res);
        public void fail(JSONObject error);
    }
}
