package com.young.music.draft.zk;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

/**
 * @author yzx
 * create_time 2020/7/16
 */
@Component
@Slf4j
public class ZkApi {



    private final ZooKeeper zkClient;

    public ZkApi(ZooKeeper zkClient) {
        this.zkClient = zkClient;
    }


    public Stat exist(String path, boolean needWatch) {
        try {
            return zkClient.exists(path, needWatch);
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Stat exist(String path, Watcher watcher) {
        try {
            return zkClient.exists(path, watcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createNode(String path, String data) {
        try {
            String s = zkClient.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("create-->" + s);
            return true;
        } catch (KeeperException | InterruptedException e) {
            log.error("创建节点失败[{}]", e.getMessage());
        }
        return false;
    }


    public boolean deleteNode(String path) {
        try {
            zkClient.delete(path, -1);
            return true;
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateNode(String path, String data) {
        try {
            zkClient.setData(path, data.getBytes(), -1);
            return true;
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getChildren(String path) {
        try {
            return zkClient.getChildren(path, false);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public String getData(String path, Watcher watcher) {
        Stat stat = new Stat();
        try {
            byte[] data = zkClient.getData(path, watcher, stat);
            return new String(data);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }




}