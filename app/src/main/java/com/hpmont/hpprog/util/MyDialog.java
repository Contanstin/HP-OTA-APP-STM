package com.hpmont.hpprog.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hpmont.hpprog.R;

public class MyDialog {
    public static void showNormalDialog(Context context,String title, String message, final ClickCallback callback){
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
        normalDialog.setIcon(R.drawable.ic_launcher_background);
        normalDialog.setTitle(title);
        normalDialog.setMessage(message);
        normalDialog.setPositiveButton(R.string.confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.clickConfirm();
                    }
                })
                .setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.clickCancel();
                    }
                });
        normalDialog.show();
    }

//    public static void showUpdataDialog(Context context, final ClickCallback callback){
//        final AlertDialog.Builder updataDialog = new AlertDialog.Builder(context);
//        updataDialog.setIcon(R.drawable.download);
//        updataDialog.setTitle(R.string.version_updata);
//        updataDialog.setMessage(R.string.updata_hint);
//        updataDialog.setCancelable(false);
//        updataDialog.setPositiveButton(R.string.updata,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        callback.clickConfirm();
//                    }
//                }).setNegativeButton(R.string.cancel,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        callback.clickCancel();
//                    }
//                });
//        updataDialog.show();
//    }

    //static AlertDialog dialog;
    public static void showUpdataDialog(Context context, String new_version, final ClickCallback callback){
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_updata, null);
        ImageView update_img = view.findViewById(R.id.update_title_icon);
        TextView update_title = view.findViewById(R.id.update_title_text);
        TextView update_message = view.findViewById(R.id.update_message);
        TextView update_old_version = view.findViewById(R.id.update_old_version);
        TextView update_new_version = view.findViewById(R.id.update_new_version);
        Button cancel_btn = view.findViewById(R.id.update_cancel);
        Button confirm_btn = view.findViewById(R.id.update_confirm);

        update_img.setImageResource(R.drawable.download);
        update_title.setText(R.string.version_updata);
        update_message.setText(R.string.updata_hint);
        update_old_version.setText(context.getResources().getString(R.string.update_old_version) +
                "V" + CommonUtil.getAppVersionName(context));
        update_new_version.setText(context.getResources().getString(R.string.update_new_version) +
                "V" + new_version);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        //dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);//去掉圆角背景背后的棱角

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.clickCancel();
                dialog.cancel();
            }
        });
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.clickConfirm();
                dialog.cancel();
            }
        });
        dialog.show();
    }

    public static void showCallDialog(Context context, String title, String message, final ClickCallback callback){
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
        normalDialog.setIcon(R.drawable.ic_launcher_background);
        normalDialog.setTitle(title);
        normalDialog.setMessage(message);
        normalDialog.setPositiveButton(R.string.login_dial,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.clickConfirm();
                    }
                })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                callback.clickCancel();
                            }
                        });
        normalDialog.show();
    }

    public interface ClickCallback {
        public void clickConfirm();
        public void clickCancel();
    }
}
