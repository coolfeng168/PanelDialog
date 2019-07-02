package com.yunfeng.paneldialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.animation.ArgbEvaluatorCompat;

import java.util.List;

/**
 * Author: xueyunfeng
 * About:  1.弹窗面板，支持全屏，拖拽关闭，多级面板（必须继承BasePanelFragment），面板高度由各个面板控制
 *         2.弹窗的打开关闭监听，在打开弹窗页面的onActivityResult()方法监听,
 *           requestCode = PanelConstants.REQUEST_CODE_PANEL_DIALOG,
 *           data.getBooleanExtra(PanelConstants.KEY_IS_PANEL_EXPANDED, false);
 *         3.必须new PanelDialog.Builder()来调用，方便统一管理
 */
public class PanelDialog extends AppCompatActivity
        implements PanelConstants, KeyboardHeightObserver {

    private CoordinatorLayout coordinatorLayout;
    private MaxHeightFrameLayout layoutPanelContent;
    private boolean isFullPanel;//是否全屏
    private boolean draggable;//是否可拖拽
    private boolean outsideTouchable;//点击面板外部是否关闭面板
    private String panelClassName;//面板内容fragment的类名
    private Bundle panelArguments;//传递给面板fragment的参数，可以为空
    private int minHeight;//当前面板最小高度
    private int maxHeight;//当前面板最大高度
    private int fixedHeight;//当前面板固定高度
    private KeyboardHeightProvider keyboardHeightProvider;//键盘高度监听

    private BottomSheetBehavior2 behavior;
    private boolean isFirst;//是否第一次启动


    private static void actionActivityForResult(Builder builder) {
        if (builder == null) {
            return;
        }
        Intent intent = new Intent(builder.activity, PanelDialog.class);
        intent.putExtra(KEY_IS_FULL_PANEL, builder.isFullPanel);
        intent.putExtra(KEY_DRAGGABLE, builder.draggable);
        intent.putExtra(KEY_OUTSIDE_TOUCHABLE, builder.outsideTouchable);
        intent.putExtra(KEY_PANEL_CLASS_NAME, builder.panelFragmentBuilder.getPanelClassName());
        intent.putExtra(KEY_PANEL_ARGUMENTS, builder.panelFragmentBuilder.getPanelArguments());
        intent.putExtra(KEY_PANEL_MIN_HEIGHT, builder.panelFragmentBuilder.getMaxHeight());
        intent.putExtra(KEY_PANEL_MAX_HEIGHT, builder.panelFragmentBuilder.getMinHeight());
        intent.putExtra(KEY_PANEL_FIXED_HEIGHT, builder.panelFragmentBuilder.getFixedHeight());
        builder.activity.startActivityForResult(intent, REQUEST_CODE_PANEL_DIALOG);
        builder.activity.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {//清空缓存的fragment
            int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
            if (backStackEntryCount > 0) {
                for (int i = 0; i < backStackEntryCount; i++) {
                    getSupportFragmentManager().popBackStackImmediate();
                }
            }
        }
        setContentView(R.layout.activity_panel);
        parseData();
        initViews();
        setListener();
        isFirst = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        keyboardHeightProvider.setKeyboardHeightObserver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        keyboardHeightProvider.setKeyboardHeightObserver(null);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            behavior.setState(BottomSheetBehavior2.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isFirst) {
            isFirst = false;
            coordinatorLayout.post(new Runnable() {
                @Override
                public void run() {
                    behavior.setState(BottomSheetBehavior2.STATE_EXPANDED);
                    keyboardHeightProvider.start();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (behavior != null) {
            behavior.onRecycle();
        }
        keyboardHeightProvider.close();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void parseData() {
        isFullPanel = getIntent().getBooleanExtra(KEY_IS_FULL_PANEL, false);
        draggable = getIntent().getBooleanExtra(KEY_DRAGGABLE, false);
        outsideTouchable = getIntent().getBooleanExtra(KEY_OUTSIDE_TOUCHABLE, true);
        panelClassName = getIntent().getStringExtra(KEY_PANEL_CLASS_NAME);
        panelArguments = getIntent().getBundleExtra(KEY_PANEL_ARGUMENTS);
        minHeight = getIntent().getIntExtra(KEY_PANEL_MIN_HEIGHT, 0);
        maxHeight = getIntent().getIntExtra(KEY_PANEL_MAX_HEIGHT, 0);
        fixedHeight = getIntent().getIntExtra(KEY_PANEL_FIXED_HEIGHT, 0);
    }

    private void initViews() {
        if (!TextUtils.isEmpty(panelClassName)) {
            Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), panelClassName);
            if (fragment instanceof BasePanelFragment) {//必须继承BasePanelFragment
                BasePanelFragment panelFragment = (BasePanelFragment) fragment;
                if (panelArguments == null) {
                    panelArguments = new Bundle();
                }
                panelArguments.putInt(KEY_PANEL_MAX_HEIGHT, maxHeight);
                panelArguments.putInt(KEY_PANEL_MIN_HEIGHT, minHeight);
                panelArguments.putInt(KEY_PANEL_FIXED_HEIGHT, fixedHeight);
                panelArguments.putBoolean(KEY_IS_FULL_PANEL, isFullPanel);
                panelArguments.putBoolean(KEY_DRAGGABLE, draggable);
                panelArguments.putBoolean(KEY_IS_TOP_LEVEL_PANEL, true);//一级面板
                panelFragment.setArguments(panelArguments);
                panelFragment.setCallback(callback);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.layout_panel_content, panelFragment);
                ft.addToBackStack(null);
                ft.commitAllowingStateLoss();
            } else {
                finish();
                return;
            }
        } else {
            finish();
            return;
        }
        layoutPanelContent = findViewById(R.id.layout_panel_content);
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        behavior = (BottomSheetBehavior2) BottomSheetBehavior2.from(layoutPanelContent);
        behavior.setPeekHeight(0);
        behavior.setState(BottomSheetBehavior2.STATE_COLLAPSED);
        behavior.setSkipCollapsed(true);
        behavior.setDraggable(draggable);

        ViewGroup.LayoutParams layoutParams = layoutPanelContent.getLayoutParams();
        if (isFullPanel) {//全屏
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        layoutPanelContent.setLayoutParams(layoutParams);

        keyboardHeightProvider = new KeyboardHeightProvider(this);
    }

    private void setListener() {
        coordinatorLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (outsideTouchable) {
                    behavior.setState(BottomSheetBehavior2.STATE_COLLAPSED);
                }
            }
        });
        behavior.setBottomSheetCallback(new BottomSheetBehavior2.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior2.STATE_COLLAPSED:
                        Intent intent = new Intent();
                        intent.putExtra(KEY_IS_PANEL_EXPANDED, false);
                        setResult(RESULT_OK, intent);
                        finish();
                        break;
                    case BottomSheetBehavior2.STATE_EXPANDED:
                        Intent intent1 = new Intent();
                        intent1.putExtra(KEY_IS_PANEL_EXPANDED, true);
                        setResult(RESULT_OK, intent1);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                BasePanelFragment fragment = getCurPanelFragment();
                if (fragment != null) {
                    fragment.onSlide(slideOffset);
                }
                //颜色渐变
                ArgbEvaluatorCompat argbEvaluatorCompat = ArgbEvaluatorCompat.getInstance();
                int color = argbEvaluatorCompat.evaluate(slideOffset, ContextCompat.getColor(PanelDialog.this, R.color.colorTransparent), ContextCompat
                        .getColor(PanelDialog.this, R.color.colorBgDialog));
                coordinatorLayout.setBackgroundColor(color);
            }
        });
        final View panelBgView = findViewById(R.id.panel_bg_view);
        //面板底部背景色
        layoutPanelContent.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int bgViewTop = panelBgView.getTop();
                int bottomSheetTop = layoutPanelContent.getTop();
                if (bgViewTop != bottomSheetTop) {
                    ViewCompat.offsetTopAndBottom(panelBgView, bottomSheetTop - bgViewTop + Utils
                            .dp2px(PanelDialog.this, 10));
                }
                return true;
            }
        });
    }

    private Callback callback = new Callback() {

        @Override
        public void changePanel(View view) {
            behavior.setCurPanelView(view);
        }

        @Override
        public void changeHeight(final int minHeight, final int maxHeight) {
            behavior.setNeedHeightChangeAnimation(true);
            layoutPanelContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    behavior.setNeedHeightChangeAnimation(true);
                    layoutPanelContent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    int min;
                    if (minHeight == 0) {
                        min = Utils.dp2px(PanelDialog.this, 250);//默认最小高度为250dp
                    } else {
                        min = minHeight;
                    }
                    int max;
                    if (maxHeight == 0) {//默认最大高度为距离屏幕的1/8
                        max = Utils.displayHeight(PanelDialog.this) * 7 / 8;
                    } else {
                        max = maxHeight;
                    }
                    max = Math.min(max, Utils.displayHeight(PanelDialog.this) * 7 / 8);//最大高度不超过屏幕的7/8
                    layoutPanelContent.setMaxHeight(max);
                    layoutPanelContent.setMinimumHeight(min);
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) layoutPanelContent.getLayoutParams();
                    layoutParams.topMargin = 0;
                    layoutParams.height = ViewGroup.MarginLayoutParams.WRAP_CONTENT;
                    layoutPanelContent.setLayoutParams(layoutParams);
                }
            });
        }

        @Override
        public void changeHeight(int fixedHeight) {
            behavior.setNeedHeightChangeAnimation(true);
            layoutPanelContent.setMinimumHeight(0);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) layoutPanelContent.getLayoutParams();
            layoutParams.topMargin = 0;
            layoutParams.height = fixedHeight;
            layoutPanelContent.setLayoutParams(layoutParams);
        }
    };

    /**
     * 键盘高度变化
     * @param height        The height of the keyboard in pixels
     * @param orientation   The orientation either: Configuration.ORIENTATION_PORTRAIT or
     */
    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        BasePanelFragment fragment = getCurPanelFragment();
        if (fragment != null) {
            fragment.onKeyboardHeightChanged(height > 0, height);
        }
    }

    /**
     * 获取当前的面板
     * @return
     */
    private BasePanelFragment getCurPanelFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for(Fragment fragment : fragments) {
            if (fragment instanceof BasePanelFragment) {
                BasePanelFragment basePanelFragment = (BasePanelFragment) fragment;
                if (basePanelFragment.isShowing()) {
                    return basePanelFragment;
                }
            }
        }
        return null;
    }

    /**
     * 链式调用生成弹窗
     */
    public static class Builder {
        private Activity activity;
        private boolean isFullPanel;//是否全屏
        private boolean draggable;//是否可拖拽
        private boolean outsideTouchable = true;//点击面板外部是否关闭面板
        private BasePanelFragment.Builder panelFragmentBuilder;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder setFullPanel(boolean fullPanel) {
            isFullPanel = fullPanel;
            return this;
        }

        public Builder setDraggable(boolean draggable) {
            this.draggable = draggable;
            return this;
        }

        public Builder setPanelFragmentBuilder(BasePanelFragment.Builder panelFragmentBuilder) {
            this.panelFragmentBuilder = panelFragmentBuilder;
            return this;
        }

        public Builder setOutsideTouchable(boolean outsideTouchable) {
            this.outsideTouchable = outsideTouchable;
            return this;
        }

        public void show() {
            actionActivityForResult(this);
        }
    }


    public interface Callback {
        void changePanel(View view);//切换面板
        void changeHeight(int minHeight, int maxHeight);//面板高度变化
        void changeHeight(int fixedHeight);//面板高度变化
    }

}
