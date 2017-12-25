# DragDetailLayout
仿京东商品详情页面上拉进入详情.

* 控件继承自ViewGroup,上下两个页面没有使用ScrollView,而是使用了两个Fragment,更方便的控制生命周期.
* 支持WebView,ListView,RecyclerView以及ViewPager
* 分别提供两种实现方案:Scroller实现和ViewDragHeler实现.并且ViewDragHelper实现的方案支持滑动阻尼效果.
