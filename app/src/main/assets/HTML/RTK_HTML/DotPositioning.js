document.addEventListener('DOMContentLoaded', (event) => {
    const socket = new WebSocket('ws://8.137.81.229:8880');  // 创建WebSocket连接，使用实际的地址

    let map;  // 地图对象
    let markers = [];  // 地图标记对象数组
    let polylines = []; // 线条对象数组

    // WebSocket连接打开时的事件处理
    socket.addEventListener('open', (event) => {
        console.log('Connected to WebSocket server.');  // 控制台输出连接成功消息
    });

    // 接收到WebSocket消息时的事件处理
    socket.addEventListener('message', (event) => {
        try {
            const data = JSON.parse(event.data);  // 解析收到的JSON数据
            if ('lon' in data && 'lat' in data) {  // 检查是否包含经度和纬度信息

                var bd09 = wgs84ToBd09(data.lon, data.lat);
                updateMap(bd09[0], bd09[1]);  // 更新地图显示位置

                //更新信息框
                updateTextContent(data);
            }
        } catch (error) {
            console.error('Error parsing JSON:', error);  // 解析JSON出错时打印错误信息
        }
    });

    // 定义更改文本内容的函数
    function updateTextContent(data) {
        var bd09 = wgs84ToBd09(data.lon, data.lat);
        document.getElementById('LonInfo').innerText = 'lon:' + bd09[0].toFixed(10);
        document.getElementById('LatInfo').innerText = 'lat:' + bd09[1].toFixed(10);
        document.getElementById('m_sInfo').innerText = '速度(m/s): ' + (data.speed / 3.6).toFixed(4);
        document.getElementById('km_hInfo').innerText = '速度(km/h): ' + data.speed.toFixed(4);

        document.getElementById('PositModeInfo').innerText = '定位模式: ' + getRTKStateText(data.rtksta);
    }

    // 根据RTK状态值返回对应的文本
    function getRTKStateText(rtkState) {
        switch (rtkState) {
            case 0:
                return '无定位';
            case 1:
                return '单点定位';
            case 2:
                return '亚米级定位';
            case 4:
                return 'RTK固定解';
            case 5:
                return 'RTK浮动解';
            default:
                return '未知';
        }
    }

    // 点击发送消息按钮时的事件处理（这部分是示例，您可以根据需要处理发送消息的逻辑）
    SaveCoordinateButton.addEventListener('click', () => {
        // 构建 JSON 对象
        var data = {n1: 99,n2: 99,n3: 99,n4: 99,z1: 99,z2: 99,z3: 99,z4: 99,s1: 99,s2: 99,s3: 99,s4: 99};

        // 将 JSON 对象转换为字符串
        var jsonData = JSON.stringify(data);

        // 在控制台输出发送的数据
        console.log("发送的数据：", jsonData);

        // 发送数据到 WebSocket 服务器
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.send(jsonData);
        } else {
            console.error('WebSocket 连接未打开。');
        }
    });

    // 点击地图按钮时的事件处理，使地图居中到最新的点
    CenterMapButton.addEventListener('click', () => {
        if (markers.length > 0) {
            const latestMarker = markers[markers.length - 1];  // 获取最新的地图标记
            map.panTo(latestMarker.getPosition());  // 将地图中心点移动到最新标记的位置
        }
    });

    ClearMapButton.addEventListener('click', () => {
       if (markers.length > 0) {
           markers.length=0;
           polylines.forEach(polyline => map.removeOverlay(polyline));
           map.clearOverlays(); // 清除地图上的所有覆盖物
       }
   });

    // WebSocket连接错误时的事件处理
    socket.addEventListener('error', (event) => {
        console.error('WebSocket error:', event);  // 控制台输出WebSocket错误信息
    });


    function wgs84ToGcj02(lon, lat) {
        var pi = 3.1415926535897932384626;
        var a = 6378245.0;
        var ee = 0.00669342162296594323;

        function transformLat(x, y) {
            var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
            ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
            ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
            return ret;
        }

        function transformLon(x, y) {
            var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
            ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
            ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
            return ret;
        }

        var dLat = transformLat(lon - 105.0, lat - 35.0);
        var dLon = transformLon(lon - 105.0, lat - 35.0);
        var radLat = lat / 180.0 * pi;
        var magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        var sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        var mgLat = lat + dLat;
        var mgLon = lon + dLon;

        return [mgLon, mgLat];
    }

    function gcj02ToBd09(lon, lat) {
        var x_pi = 3.14159265358979324 * 3000.0 / 180.0;
        var z = Math.sqrt(lon * lon + lat * lat) + 0.00002 * Math.sin(lat * x_pi);
        var theta = Math.atan2(lat, lon) + 0.000003 * Math.cos(lon * x_pi);
        var bd_lon = z * Math.cos(theta) + 0.0065;
        var bd_lat = z * Math.sin(theta) + 0.006;

        return [bd_lon, bd_lat];
    }
    function wgs84ToBd09(lon, lat) {
        var gcj02 = wgs84ToGcj02(lon, lat);
        return gcj02ToBd09(gcj02[0], gcj02[1]);
    }


    // 更新地图显示位置的函数
    function updateMap(lon, lat) {
        const point = new BMapGL.Point(lon, lat);  // 创建百度地图的坐标点对象
        if (!map) {  // 如果地图对象不存在
            map = new BMapGL.Map('map');  // 创建新的百度地图对象并指定容器
            map.centerAndZoom(point, 15);  // 设置地图中心点和缩放级别
            map.enableScrollWheelZoom(true);  // 启用地图的鼠标滚轮缩放功能
        }

        // 添加新的标记到地图上
        const marker = new BMapGL.Marker(point);  // 创建地图标记对象
        marker.setIcon(new BMapGL.Icon('https://maps.baidu.com/common/openapi/images/markers_new/1.png', new BMapGL.Size(10, 10))); // 设置标记图标

        // 检查连续点相同的情况，如果连续点相同则不添加到地图和数组中
        if (markers.length > 0) {
            const lastMarker = markers[markers.length - 1];
            if (lastMarker.getPosition().equals(marker.getPosition())) {
                return;  // 如果连续点相同，则直接返回，不添加标记和路径
            }
        }

        map.addOverlay(marker);  // 将标记添加到地图上
        markers.push(marker);  // 将标记对象添加到数组中

        // 绘制连接线条
        if (markers.length > 1) {
            // 只绘制新的线条
            const startMarker = markers[markers.length - 2];
            const endMarker = markers[markers.length - 1];

            // 检查相邻点是否相同，跳过相同的点
            if (!startMarker.getPosition().equals(endMarker.getPosition())) {
                // 绘制最新的路径段（红色）
                const lastPolyline = new BMapGL.Polyline([
                    startMarker.getPosition(),
                    endMarker.getPosition()
                ], {strokeColor: "#FF0000", strokeWeight: 5, strokeOpacity: 0.8});  // 设置线条的样式为红色
                map.addOverlay(lastPolyline);  // 将线条添加到地图上
                polylines.push(lastPolyline);  // 将线条对象添加到数组中
            }
        }
    }

    // 初始化百度地图显示位置（设置为默认位置）
    updateMap(106.6185742, 26.3856009);  // 默认显示的地点
});
