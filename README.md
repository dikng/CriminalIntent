# CriminalIntent
  该项目为《Android编程权威指南》上的实例，主要实现的功能类似于简单记事本。
### 已完成  
  - 完成书本7-19章的基本功能
  - 在CrimePageActivity 中增加跳转按钮，可直接跳转至第一条和最后一条crime
  - 增加在CrimeFragment中删除当前Crime的功能
  - 增加Crime为空时的空白提示和添加新Crime快捷按钮
  - 增加拨打嫌疑人电话功能
  - 使用PhotoFragment替换DisplaPhotoyActivity，以弹窗方式展示图片详情
  - 增加滑动删除和拖拽移动，但拖拽只是改变当前数据库视图，数据库中数据没有发生改变

### 未解决Bug
  - ~~删除非末端Crime记录时，会出现软件崩溃~~

### 需要优化
  - 目前无论是增加Crime还是修改已有Crime都在updateUi()中调用notifyDataSetChanged()更新全部ViewHolder，
    但调用mAdapter.notifyItemChanged(clickedPosition)会在删除非末端Crime记录时，因为clickedPositon可
    能溢出而导致程序崩溃。需要根据对CrimeFragment中是删除还是修改进行判断，在选择相关刷新操作。

