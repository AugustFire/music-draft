package com.young.music.draft.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

/**
 * @author yzx
 * create_time 2020/7/17
 */
public class Curator {
    public static void main(String[] args) throws Exception {
        String connectionStr = "localhost";
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectionStr, retryPolicy);
        curatorFramework.start();
        // String s = curatorFramework.create()
        //         .creatingParentsIfNeeded()
        //         .withMode(CreateMode.PERSISTENT)
        //         .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
        //         .forPath("/node/child_0", "123".getBytes());
        // System.out.println(s);
        InterProcessMutex interProcessMutex = new InterProcessMutex(curatorFramework, "/root_lock");
        interProcessMutex.acquire();
        int read = System.in.read();
        System.out.println(read);
        interProcessMutex.acquire();
    }
}
