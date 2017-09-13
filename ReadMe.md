##门口屏说明
	
 master分支-医院通用的方式
 ShangDong分支-山东医院定制，UI不同
 
 ***使用MQTT协议代替Netty实现通信***
 	
 	模块build文件添加如下依赖
 	 compile 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.1.0'
     compile 'org.eclipse.paho:org.eclipse.paho.android.service:1.1.1'
     manifest文件添加
     <service android:name="org.eclipse.paho.android.service.MqttService"/>
 
      
  ***使用android架构组件LifeCycle，ViewModel Room***