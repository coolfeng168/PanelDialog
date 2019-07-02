package com.yunfeng.paneldialog.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yunfeng.paneldialog.BasePanelFragment;
import com.yunfeng.paneldialog.R;

/**
 * Author: xueyunfeng
 * About:  全屏面板1
 */
public class TestFullPanelFragment1 extends BasePanelFragment {
    private TextView tvKeyboard;
    /**
     * 生成Builder
     * @param bundle
     * @return
     */
    public static Builder newBuilder(Context context, Bundle bundle) {
        return new Builder()
                //.setFixedHeight() 全屏面板不用传固定高度，传了也无效
                //.setMaxHeight() 全屏面板不用传最大高度，传了也无效
                //.setMinHeight() 全屏面板不用传最小高度，传了也无效
                .setPanelArguments(bundle) //需要传递给面板的业务参数
                .setPanelClassName(TestFullPanelFragment1.class.getName());
    }

    @Override
    protected int getContentLayoutResId() {
        return R.layout.fragment_test_full_panel1;
    }

    @Override
    protected void initViews() {
        Button btn = (Button) getView().findViewById(R.id.btn_next);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionNextPanel(TestFullPanelFragment2.newBuilder(getContext(), null));
            }
        });
        tvKeyboard = (TextView) getView().findViewById(R.id.tv_keyboard);
    }

    @Override
    protected void show() {

    }

    @Override
    protected void hide() {

    }

    @Override
    public void onKeyboardHeightChanged(boolean isKeyboardShow, int keyboardHeight) {
        super.onKeyboardHeightChanged(isKeyboardShow, keyboardHeight);
        if (isKeyboardShow) {
            tvKeyboard.setText("键盘打开啦！！！ 高度为" + keyboardHeight);
        } else {
            tvKeyboard.setText("键盘关闭啦！！！");
        }
    }
}
