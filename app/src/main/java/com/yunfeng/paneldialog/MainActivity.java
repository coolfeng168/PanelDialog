package com.yunfeng.paneldialog;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.yunfeng.paneldialog.fragment.TestFullPanelFragment1;
import com.yunfeng.paneldialog.fragment.TestPanelFragment1;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initListener();
    }

    private void initListener() {
        findViewById(R.id.tv_panel).setOnClickListener(this);
        findViewById(R.id.tv_panel_draggable).setOnClickListener(this);
        findViewById(R.id.tv_panel_outside).setOnClickListener(this);
        findViewById(R.id.tv_full_panel).setOnClickListener(this);
        findViewById(R.id.tv_full_panel_draggable).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_panel:
                new PanelDialog.Builder(this)
                        .setDraggable(false)//是否可拖拽，默认为false
                        .setFullPanel(false)//是否全屏，默认为false
                        .setOutsideTouchable(true)//点击外部是否可以关闭dialog，默认为true
                        .setPanelFragmentBuilder(TestPanelFragment1.newBuilder(this, null))
                        .show();
                break;
            case R.id.tv_panel_draggable:
                new PanelDialog.Builder(this)
                        .setDraggable(true)//是否可拖拽，默认为false
                        .setFullPanel(false)//是否全屏，默认为false
                        .setOutsideTouchable(true)//点击外部是否可以关闭dialog，默认为true
                        .setPanelFragmentBuilder(TestPanelFragment1.newBuilder(this, null))
                        .show();
                break;
            case R.id.tv_panel_outside:
                new PanelDialog.Builder(this)
                        .setDraggable(true)//是否可拖拽，默认为false
                        .setFullPanel(false)//是否全屏，默认为false
                        .setOutsideTouchable(false)//点击外部是否可以关闭dialog，默认为true
                        .setPanelFragmentBuilder(TestPanelFragment1.newBuilder(this, null))
                        .show();
                break;
            case R.id.tv_full_panel:
                new PanelDialog.Builder(this)
                        .setDraggable(false)//是否可拖拽，默认为false
                        .setFullPanel(true)//是否全屏，默认为false
                        .setOutsideTouchable(false)//点击外部是否可以关闭dialog，默认为true
                        .setPanelFragmentBuilder(TestFullPanelFragment1.newBuilder(this, null))
                        .show();
                break;
            case R.id.tv_full_panel_draggable:
                new PanelDialog.Builder(this)
                        .setDraggable(true)//是否可拖拽，默认为false
                        .setFullPanel(true)//是否全屏，默认为false
                        .setOutsideTouchable(false)//点击外部是否可以关闭dialog，默认为true
                        .setPanelFragmentBuilder(TestFullPanelFragment1.newBuilder(this, null))
                        .show();
                break;
        }
    }
}
