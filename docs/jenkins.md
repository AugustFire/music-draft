# jenkins

## 1.安装
```text
    1.官网去下载jenkins的最新war包 `jenkins.war` 
    2.下载tomcat `apache-tomcat-9.0.36.tar.gz`                      
    3.jenkins.war放tomcat并启动(默认端口8080)
    4.jenkins 插件安装很慢加速
    	1.update.xml -> http://mirror.esuni.jp/jenkins/updates/update-center.json
    	2.updates/default.json
    		# sed -i 's/www.google.com/www.baidu.com/g' default.json
    		# sed -i 's/updates.jenkins-ci.org\/download/mirrors.tuna.tsinghua.edu.cn\/jenkins/g' default.json
    5.配置nginx代理8080
    notice:最好不使用docker部署jenkins,因为jenkins需要读取宿主机目录/命令,避免docker挂载太多环境变量,或考虑自定义image 
```
## 2.配置
```text
    1.安装推荐插件
        - Publish over SSH  :推送到远程服务器
        - Maven Integration plugin  :构建maven项目插件
        - Gradle Plugin     :构建gradle项目执行build脚本
    2.系统配置: 
        - 配置远程服务器ssh
        - 配置邮件通知
    3.全局工具配置:
        - git:  拉取git repo代码到宿主机workspace
        - maven:    编译打包
        - jdk:      jre env
    4.凭证管理
           -访问git仓库的秘钥

```

## 3.使用demo
```text
    1.创建maven项目
    2.配置项目访问git repo的凭证
    3.配置编译命令: clean install -pl ${project_name} -am -amd -Pdev -Dmaven.test.skip=true
    4.post step

        -> BUILD_ID=dontKillMe
        -> cd /www/app/xxx
        -> ./setup.sh
```

## 4.宿主机部署,未推送到远程服务器
```shell script
#//bin/bash
#Program:
#	This program is execute by jenkins to run springboot jar applicaiton.
#History:
#2020/06/30 AugustFire First
JAVA_PID="`jps -l |grep xx-server |cut -d ' ' -f 1 `"
echo "kill the pid $JAVA_PID for jar!"
kill -9 $JAVA_PID
echo "springboot jar is on start up!"
rm -f *.jar
cp /root/.jenkins/workspace/xx/xx-server/target/xx-server-0.0.1-SNAPSHOT.jar ./
nohup java -jar -Dspring.profiles.active=base,prod  *.jar >/dev/null 2>&1 &
echo "Ok~"

```

 