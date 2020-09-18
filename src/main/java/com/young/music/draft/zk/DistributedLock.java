package com.young.music.draft.zk;


import lombok.Getter;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author yzx
 * create_time 2020/7/16
 */
public class DistributedLock implements Lock, Watcher {

    @Getter
    private ZooKeeper zk;
    private static final String ROOT_LOCK = "/locks";
    private String CURRENT_LOCK;
    private String WAITING_LOCK;
    private CountDownLatch countDownLatch;


    public DistributedLock() {
        try {
            zk = new ZooKeeper("localhost:2181", 50000, this);
            Stat stat = zk.exists(ROOT_LOCK, false);
            if (stat == null) {
                zk.create(ROOT_LOCK, "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void lock() {
        if (tryLock()) {
            System.out.println(Thread.currentThread().getName() + "->" + CURRENT_LOCK + "获得锁成功");
            return;
        }
        waitForLock(WAITING_LOCK);
        System.out.println(Thread.currentThread().getName() + "->获得锁成功");
    }


    private void waitForLock(String prevLock) {
        try {
            //监听上一个节点
            System.out.println("prevLock------------->" + prevLock);
            Stat stat = zk.exists(prevLock, true);// watch上一个节点
            if (stat != null) {
                System.out.println(Thread.currentThread().getName() + "->等待锁" + ROOT_LOCK + "/+" + prevLock + "释放");
                countDownLatch = new CountDownLatch(1);

                countDownLatch.await();
            }

        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        //创建临时有序 -e -s
        try {
            CURRENT_LOCK = zk.create(ROOT_LOCK + "/", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(Thread.currentThread().getName() + "-->" + CURRENT_LOCK + "-->[try lock!]");
            List<String> children = zk.getChildren(ROOT_LOCK, false);
            Collections.sort(children);
            if ((ROOT_LOCK + "/" + children.get(0)).equals(CURRENT_LOCK)) {
                return true;
            }
            for (int i = 0; i < children.size(); i++) {
                if ((ROOT_LOCK + "/" + children.get(i)).equals(CURRENT_LOCK)) {
                    WAITING_LOCK = ROOT_LOCK + "/" + children.get(i - 1);
                }
            }

        } catch (KeeperException | InterruptedException e) {
            System.out.println("error-_>ce");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        System.out.println(Thread.currentThread().getName() + "释放锁" + CURRENT_LOCK);
        try {
            zk.delete(CURRENT_LOCK, -1);
            CURRENT_LOCK = null;
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public void process(WatchedEvent event) {
        if (countDownLatch != null) {
            System.out.println(Thread.currentThread().getName() + "---process--->" + event.getPath() + event.getType());
            try {
                List<String> children = zk.getChildren(ROOT_LOCK, false);
                Collections.sort(children);
                if ((ROOT_LOCK + "/" + children.get(0)).equals(CURRENT_LOCK)) {
                    //获得锁成功 -> 放行
                    this.countDownLatch.countDown();
                    return;
                }
                for (int i = 0; i < children.size(); i++) {
                    System.out.println("*************");
                    if ((ROOT_LOCK + "/" + children.get(i)).equals(CURRENT_LOCK)) {
                        WAITING_LOCK = ROOT_LOCK + "/" + children.get(i - 1);
                        waitForLock(WAITING_LOCK);
                        // this.countDownLatch.countDown();
                    }
                }

            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
