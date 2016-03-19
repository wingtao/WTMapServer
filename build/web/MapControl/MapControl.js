/**
 * 
 * @param {type} opts
 * @returns {undefined}
 * Usage: 
 *  var map = new MapControl({
 *      div: "map",
 *      minx: 100,
 *      maxx: 120,
 *      miny: 30,
 *      maxy : 50
 *      width:400,
 *      height: 300
 *  });
 */
MapControl = function (opts) {          //函数表达式 类似于C++中的类
    var me = this;                      //防止因闭包等其他情况this指针改变
    me.opts = $.extend({        //设置图片显示缺省值
        minx: 75,
        maxx: 135,
        miny: 20,
        maxy: 60,
        width: 800,
        height: 600
    }, opts);
    
    
    //自定义
    me.usrID=4214;
    
    
    
    //坐标变换
    me.opts.centerX = (me.opts.maxx + me.opts.minx) * 0.5;
    me.opts.centerY = (me.opts.miny + me.opts.maxy) * 0.5;
    var sx = me.opts.width / (me.opts.maxx - me.opts.minx);
    var sy = me.opts.height / (me.opts.maxy - me.opts.miny);
    me.opts.scale = sx > sy ? sy : sx;
    
    me.div = $("#" + me.opts.div).css({
        width: me.opts.width + "px",
        height: me.opts.height + "px"
    });
    var html = "<img id='img' style='width:100%; height:100%;'>";
    me.div.html(html);
    me.img = $("#img");

    me.recaculateExtent();
    me.refresh();

};

MapControl.prototype.changeCoor=function(){
    var me=this;
    var usrSrid=$("#corID").val();
    if(usrSrid!=null&&usrSrid!=undefined&&usrSrid!=""){
        me.usrID=usrSrid;
        me.recaculateExtent();
        me.refresh();
    }else{
        alert("请先输入！");
    };
};
//添加函数refresh等函数方法到原型中

//根据改变后的条件刷新图片
MapControl.prototype.refresh = function () {
    var me = this;
    var w = me.img.width();
    var h = me.img.height();
    var uID=me.usrID;

    me.img.attr("src", "MapServer?width=" + w + "&height=" + h + "&minx=" + me.opts.minx + "&maxx=" + me.opts.maxx + "&miny=" + me.opts.miny + "&maxy=" + me.opts.maxy + "&format=png"+"&srid="+uID);

};
//重算区域范围
MapControl.prototype.recaculateExtent = function () {
    var me = this;

    me.opts.minx = me.opts.centerX - me.opts.width * 0.5 / me.opts.scale;
    me.opts.maxx = me.opts.centerX + me.opts.width * 0.5 / me.opts.scale;
    me.opts.miny = me.opts.centerY - me.opts.height * 0.5 / me.opts.scale;
    me.opts.maxy = me.opts.centerY + me.opts.height * 0.5 / me.opts.scale;
};

//实现放大和缩小功能
//放大
MapControl.prototype.zoomIn = function(){
   var me = this;
   me.opts.scale *= 2;
   me.recaculateExtent();
   me.refresh();
};
//缩小
MapControl.prototype.zoomOut = function(){
   var me = this;
   me.opts.scale *= 0.5;
   me.recaculateExtent();
   me.refresh();
};

//个人补充完善的向上、下、左、右调整的函数
MapControl.prototype.left = function(){
    var me=this;
    me.opts.minx += 1;
    me.opts.maxx += 1;    
    me.refresh();
};
MapControl.prototype.right = function(){
    var me=this;
    me.opts.minx -= 1;
    me.opts.maxx -= 1;
    me.refresh();
};
MapControl.prototype.up = function(){
    var me=this;
    me.opts.miny -= 1;
    me.opts.maxy -= 1;
    me.refresh();
};
MapControl.prototype.down = function(){
    var me=this;
    me.opts.miny += 1;
    me.opts.maxy += 1;
    me.refresh();
};

MapControl.prototype.setCenter=function(coord){
    var me = this;
    me.opts.centerX = coord.lon;
    me.opts.centerY = coord.lat;
    me.recaculateExtent();
    me.refresh();
};
