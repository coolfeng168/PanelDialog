package com.yunfeng.paneldialog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Author: xueyunfeng
 * About:  面板内容基类，所有需要使用面板展示的内容都要继承此类，并实现对应的方法，否则无法使用。
 *         有两种面板：
 *         1.全屏面板
 *         2.半屏面板：
 *         （1）自适应高度面板，可设置最大最小面板高度，默认最小高度为250dp，最大高度为距离屏幕顶部1/8屏幕高度
 *         （2）固定高度面板，设置固定高度
 *         3.面板内容layout，使用NestScrollView，RecyclerView 或者其他实现了NestedScrollingChild接口的类
 *         4.必须new BasePanelFragment.Builder()来调用，方便统一管理
 *         5.面板之间跳转，必须使用actionNextPanel(Builder builder)，主动关闭当前面板必须调用closePanel
 */
public abstract class BasePanelFragment extends Fragment implements PanelConstants {

    private boolean isShowing;//当前面板是不是正在显示
    private boolean isTopLevelPanel;//是否是一级面板
    private boolean isFullPanel;//是否全屏
    private boolean draggable;//是否可拖拽
    private int minHeight;//最小高度
    private int maxHeight;//最大高度
    private int fixedHeight;//固定高度，设置时有

    private PanelDialog.Callback callback;

    /**
     * 设置面板回调
     * @param callback
     */
    public void setCallback(PanelDialog.Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable
            ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base_panel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FrameLayout layoutContent = view.findViewById(R.id.layout_content);
        LayoutInflater.from(getContext()).inflate(getContentLayoutResId(), layoutContent, true);
        initViews();
        isShowing = true;
        changePanel();
        show();
    }

    private void parseData() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            isTopLevelPanel = arguments.getBoolean(KEY_IS_TOP_LEVEL_PANEL, false);
            isFullPanel = arguments.getBoolean(KEY_IS_FULL_PANEL, false);
            draggable = arguments.getBoolean(KEY_DRAGGABLE, false);
            minHeight = arguments.getInt(KEY_PANEL_MIN_HEIGHT, 0);
            maxHeight = arguments.getInt(KEY_PANEL_MAX_HEIGHT, 0);
            fixedHeight = arguments.getInt(KEY_PANEL_FIXED_HEIGHT, 0);
            if (minHeight > maxHeight) {
                throw new IllegalArgumentException("minHeight must be less than maxHeight");
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            isShowing = false;
            hide();
        } else {
            isShowing = true;
            changePanel();
            show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isShowing = false;
    }

    /**
     * 业务定义的面板内容布局id
     * @return
     */
    @LayoutRes
    protected abstract int getContentLayoutResId();


    /**
     * 初始化视图组建
     */
    protected abstract void initViews();

    /**
     * fragment展示
     */
    protected abstract void show();

    /**
     * fragment隐藏
     */
    protected abstract void hide();

    /**
     * 键盘状态变化，各个业务可以自己重写实现自己的逻辑（因为PanelDialog的windowSoftInputMode="adjustNothing"，所以键盘变化的统一由PanelDialog调用）
     * @param isKeyboardShow
     * @param keyboardHeight
     */
    public void onKeyboardHeightChanged(boolean isKeyboardShow, int keyboardHeight) {
    }

    /**
     * 面板的移动的监听
     * @param slideOffset 移动百分比，0表示面板关闭，1表示面板完全展开
     */
    public void onSlide(float slideOffset) {
    }

    /**
     * 面板切换
     */
    private void changePanel() {
        //面板切换，隐藏软键盘
        if (getActivity() != null && !getActivity().isFinishing()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
            }
        }

        if (callback != null) {
            callback.changePanel(getView());
            if (!isFullPanel) {//全屏不需要改变面板高度
                if (fixedHeight == 0) {//如果没有设置固定高度，则表示面板高度在最大最小高度之间自适应
                    callback.changeHeight(minHeight, maxHeight);
                } else {
                    callback.changeHeight(fixedHeight);
                }
            }
        }
    }

    /**
     * 是否正在显示该面板
     * @return
     */
    public boolean isShowing() {
        return isShowing;
    }

    /**
     * 是否是一级面板
     * @return
     */
    public boolean isTopLevelPanel() {
        return isTopLevelPanel;
    }

    /**
     * 是否全屏
     * @return
     */
    public boolean isFullPanel() {
        return isFullPanel;
    }

    /**
     * 固定高度，设置时有
     * @return
     */
    public int getFixedHeight() {
        return fixedHeight;
    }

    /**
     * 最大高度
     * @return
     */
    public int getMaxHeight() {
        return maxHeight;
    }

    /**
     * 最小高度
     * @return
     */
    public int getMinHeight() {
        return minHeight;
    }

    /**
     * 是否可拖拽
     * @return
     */
    public boolean isDraggable() {
        return draggable;
    }

    /**
     * 跳转到下一级面板
     * @param builder
     */
    public void actionNextPanel(Builder builder) {
        if (builder == null
                || getActivity() == null
                || getActivity().isFinishing()) {
            return;
        }
        if (!TextUtils.isEmpty(builder.panelClassName)) {
            Fragment fragment = getFragmentManager().getFragmentFactory().instantiate(getContext().getClassLoader(), builder.panelClassName);
            if (fragment instanceof BasePanelFragment) {//必须继承BasePanelFragment
                BasePanelFragment panelFragment = (BasePanelFragment) fragment;
                if (builder.panelArguments == null) {
                    builder.panelArguments = new Bundle();
                }
                builder.panelArguments.putInt(KEY_PANEL_MAX_HEIGHT, builder.maxHeight);
                builder.panelArguments.putInt(KEY_PANEL_MIN_HEIGHT, builder.minHeight);
                builder.panelArguments.putInt(KEY_PANEL_FIXED_HEIGHT, builder.fixedHeight);
                builder.panelArguments.putBoolean(KEY_IS_FULL_PANEL, isFullPanel);
                builder.panelArguments.putBoolean(KEY_DRAGGABLE, draggable);
                builder.panelArguments.putBoolean(KEY_IS_TOP_LEVEL_PANEL, false);//二级面板
                panelFragment.setArguments(builder.panelArguments);
                panelFragment.setCallback(callback);
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.panel_in_from_right, R.anim.panel_out_to_left, R.anim.panel_in_from_left, R.anim.panel_out_to_right)
                        .hide(this)
                        .add(R.id.layout_panel_content, panelFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }
        }
    }

    /**
     * 关闭当前面板页
     */
    public void closePanel() {
        try {
            if (getActivity() == null
                    || getActivity().isFinishing()) {
                return;
            }
            getFragmentManager().popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 链式调用方式生成面板
     */
    public static class Builder {
        private int minHeight;//最小高度
        private int maxHeight;//最大高度
        private int fixedHeight;//固定高度
        private String panelClassName;//面板内容fragment的类名
        private Bundle panelArguments;//业务传递给面板fragment的参数，可以为空

        public Builder() {

        }

        public int getMinHeight() {
            return minHeight;
        }

        public Builder setMinHeight(int minHeight) {
            this.minHeight = minHeight;
            return this;
        }

        public int getMaxHeight() {
            return maxHeight;
        }

        public Builder setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        public int getFixedHeight() {
            return fixedHeight;
        }

        public Builder setFixedHeight(int fixedHeight) {
            this.fixedHeight = fixedHeight;
            return this;
        }

        public String getPanelClassName() {
            return panelClassName;
        }

        public Builder setPanelClassName(String panelClassName) {
            this.panelClassName = panelClassName;
            return this;
        }

        public Bundle getPanelArguments() {
            return panelArguments;
        }

        public Builder setPanelArguments(Bundle panelArguments) {
            this.panelArguments = panelArguments;
            return this;
        }
    }

}
