package com.yunfeng.paneldialog.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yunfeng.paneldialog.BasePanelFragment;
import com.yunfeng.paneldialog.R;
import com.yunfeng.paneldialog.Utils;

/**
 * Author: xueyunfeng
 * About:  半屏面板，自适应高度，最大600dp，最小300dp
 */
public class TestPanelFragment4 extends BasePanelFragment {
    private TextView tvKeyboard;
    /**
     * 生成Builder
     * @param bundle
     * @return
     */
    public static Builder newBuilder(Context context, Bundle bundle) {
        return new Builder()
                //.setFixedHeight() 自适应高度，不可以设置固定高度
                .setMaxHeight(Utils.dp2px(context, 600)) //自适应高度，最大600dp
                .setMinHeight(Utils.dp2px(context,300)) //自适应高度，最小300dp
                .setPanelArguments(bundle) //需要传递给面板的业务参数
                .setPanelClassName(TestPanelFragment4.class.getName());
    }

    @Override
    protected int getContentLayoutResId() {
        return R.layout.fragment_test_panel4;
    }

    @Override
    protected void initViews() {
        Button btn = (Button) getView().findViewById(R.id.btn_next);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closePanel();
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
