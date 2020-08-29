package com.hpmont.hpprog.widget;

import java.text.NumberFormat;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hpmont.hpprog.R;

public class MyProgressDialog extends AlertDialog {
    private ProgressBar mProgress;
    private TextView mProgressDescribe;
    private TextView mProgressPercent;
    private TextView mProgressTitle;
    private Button mProgressButton;
    private Handler mViewUpdateHandler;
    private int mMax;
    private CharSequence mMessage;
    private boolean mHasStarted;
    private int mProgressVal;
    private String TAG="MyProgressDialog";
    private NumberFormat mProgressPercentFormat;
    private OnCancelClickListener onCancelClickListener;
    private String cancel;

    public MyProgressDialog(Context context) {
        super(context);
    // TODO Auto-generated constructor stub
        initFormats();
    }

    public MyProgressDialog(Context context, int themeId) {
        super(context, themeId);
        // TODO Auto-generated constructor stub
        initFormats();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
// TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;

        mProgress = (ProgressBar)findViewById(R.id.progress);
        mProgressDescribe = (TextView)findViewById(R.id.progress_describe);
        mProgressPercent = (TextView)findViewById(R.id.progress_percent);
        mProgressTitle = (TextView)findViewById(R.id.progress_title);
        mProgressButton = (Button)findViewById(R.id.progress_cancel);
        mViewUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                int progress = mProgress.getProgress();
                int max = mProgress.getMax();
                if (mProgressPercentFormat != null) {
                    double percent = (double) progress / (double) max;
                    SpannableString tmp = new SpannableString(mProgressPercentFormat.format(percent));
                    tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                            0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mProgressPercent.setText(tmp);
                }
                else {
                    mProgressPercent.setText("");
                }
            }
        };
        onProgressChanged();
        if (mMessage != null) {
            setTitle(mMessage);
        }
        if (mMax > 0) {
            setMax(mMax);
        }
        if (mProgressVal > 0) {
            setProgress(mProgressVal);
        }
        if(cancel != null){
            mProgressButton.setText(cancel);
        }

        mProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCancelClickListener != null) {
                    onCancelClickListener.onCancelClick();
                }
            }
        });
    }

    private void initFormats() {
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
    }

    private void onProgressChanged() {
        mViewUpdateHandler.sendEmptyMessage(0);
    }

    public void setCancelListener(String str, OnCancelClickListener onCancelClickListener){
        if(str != null){
            cancel = str;
        }
        this.onCancelClickListener = onCancelClickListener;
    }

    public int getMax() {
        if (mProgress != null) {
            return mProgress.getMax();
        }
        return mMax;
    }

    public void setMax(int max) {
        if (mProgress != null) {
            mProgress.setMax(max * 1024 * 1024);
            onProgressChanged();
        } else {
            mMax = max * 1024 * 1024;
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        if (mProgress != null) {
            mProgress.setIndeterminate(indeterminate);
        }
    }

    public void setProgress(int value) {
        if (mHasStarted) {
            mProgress.setProgress(value * 1024 * 1024);
            onProgressChanged();
        } else {
            mProgressVal = value * 1024 * 1024;
        }
    }

    @Override
    public void setTitle(CharSequence message) {
    // TODO Auto-generated method stub
        if(mProgressTitle != null){
            mProgressTitle.setText(message);
        }
        else{
            mMessage = message;
        }
    }

    public void appendMsg(String msg){
        if(msg != null){
            mProgressDescribe.append(msg);
        }
        else{
            mProgressDescribe.append("");
        }
    }

    public void setMessage(String message){
        if(message != null){
            mProgressDescribe.setText(message);
        }
        else{
            mProgressDescribe.setText("");
        }
    }

    @Override
    protected void onStart() {
    // TODO Auto-generated method stub
        super.onStart();
        mHasStarted = true;
    }

    @Override
    protected void onStop() {
    // TODO Auto-generated method stub
        super.onStop();
        mHasStarted = false;
    }

    public interface OnCancelClickListener {
         public void onCancelClick();
    }
}
