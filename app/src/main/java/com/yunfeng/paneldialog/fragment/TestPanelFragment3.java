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
 * About:  半屏面板，固定高度500dp
 */
public class TestPanelFragment3 extends BasePanelFragment {
    private TextView tvKeyboard;
    /**
     * 生成Builder
     * @param bundle
     * @return
     */
    public static Builder newBuilder(Context context, Bundle bundle) {
        return new Builder()
                .setFixedHeight(Utils.dp2px(context, 500)) //固定高度
                //.setMaxHeight() 如果设置了固定高度，不用传最大高度，传了也无效
                //.setMinHeight() 如果设置了固定高度，不用传最小高度，传了也无效
                .setPanelArguments(bundle) //需要传递给面板的业务参数
                .setPanelClassName(TestPanelFragment3.class.getName());
    }

    @Override
    protected int getContentLayoutResId() {
        return R.layout.fragment_test_panel3;
    }

    @Override
    protected void initViews() {
        Button btn = (Button) getView().findViewById(R.id.btn_next);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionNextPanel(TestPanelFragment4.newBuilder(getContext(), null));
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
