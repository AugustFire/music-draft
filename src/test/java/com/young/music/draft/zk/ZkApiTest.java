package com.young.music.draft.zk;

import com.young.music.draft.MusicDraftApplicationTests;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author yzx
 * create_time 2020/7/16
 */
public class ZkApiTest extends MusicDraftApplicationTests {
    //
    // @Autowired
    // private ZkApi zkApi;
    //
    // @Test
    // public void exist() throws InterruptedException {
    //     Stat exist = zkApi.exist("/tom", true);
    //     System.out.println(exist);
    //     TimeUnit.SECONDS.sleep(50);
    // }
    // @Test
    // public void existWatch() throws InterruptedException {
    //     Stat exist = zkApi.exist("/tom", new MyWatcher());
    //     System.out.println(exist);
    //     TimeUnit.SECONDS.sleep(30);
    // }
}