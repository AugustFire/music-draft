#dubbo
## 容错机制
```text
 - failover(default)    失败自动切换重试 
 - failfast             快速失败
 - failsafe             失败直接吞掉(忽略)
 - failback             记录失败情况,定时重发
 - forking              并行调用多个,有一个返回则成功
 - broadcast            广播所有调用者,有一个失败则失败
```

## 服务降级
服务降级是一段时间没有响应则给个类似系统繁忙的mock结果,可以和容错搭配使用
```xml
<dubbo:reference id="demoService" interface="""com.xxx.interface" register="zookeeper" cluster="failover" mock="xxx.xxMock" timeout="500"/>
```

## SPI
/META_INF
    /dubbo
    /internal
    /services