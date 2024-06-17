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
            if ('lng' in data && 'lat' in data) {  // 检查是否包含经度和纬度信息
                updateMap(data.lng, data.lat);  // 更新地图显示位置

                //更新信息框
                updateTextContent(data);
            }
        } catch (error) {
            console.error('Error parsing JSON:', error);  // 解析JSON出错时打印错误信息
        }
    });

    // 定义更改文本内容的函数
    function updateTextContent(data) {
        document.getElementById('LonInfo').innerText = '经度: ' + data.lng;
        document.getElementById('LatInfo').innerText = '纬度: ' + data.lat;
        document.getElementById('m_sInfo').innerText = '速度(m/s): ' + (data.speed / 3.6).toFixed(4);
        document.getElementById('km_hInfo').innerText = '速度(km/h): ' + data.speed.toFixed(4);

        document.getElementById('PositModeInfo').innerText = '定位模式: ' + getRTKStateText(data.rtkstate);
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

    // 更新地图显示位置的函数
    function updateMap(lng, lat) {
        const point = new BMapGL.Point(lng, lat);  // 创建百度地图的坐标点对象
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
