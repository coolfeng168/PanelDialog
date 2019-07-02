# PanelDialog
支持全屏和半屏模式的底部弹窗面板，支持拖拽，嵌套滑动，多级面板


demo：

![image](https://github.com/coolfeng168/PanelDialog/blob/master/demo.gif)



复制BottomSheetBehavior，做对应的修改。

使用方法：
```
new PanelDialog.Builder(this)
                        .setDraggable(true)//是否可拖拽，默认为false
                        .setFullPanel(false)//是否全屏，默认为false
                        .setOutsideTouchable(true)//点击外部是否可以关闭dialog，默认为true
                        .setPanelFragmentBuilder(TestPanelFragment1.newBuilder(this, null))
                        .show();
```

```
new Builder()
                .setFixedHeight(Utils.dp2px(context, 200)) //固定高度
                //.setMaxHeight() 如果设置了固定高度，不用传最大高度，传了也无效
                //.setMinHeight() 如果设置了固定高度，不用传最小高度，传了也无效
                .setPanelArguments(bundle) //需要传递给面板的业务参数
                .setPanelClassName(TestPanelFragment1.class.getName());
```
