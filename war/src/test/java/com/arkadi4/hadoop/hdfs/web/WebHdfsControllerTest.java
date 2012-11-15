package com.arkadi4.hadoop.hdfs.web;

import static org.testng.AssertJUnit.assertEquals;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.arkadi4.hadoop.hdfs.NativeHdfsService;
import com.arkadi4.hadoop.hdfs.model.FileInfo;

public class WebHdfsControllerTest {

    private NativeHdfsService hdfsService = null;
    private String testDirectory = "/" + WebHdfsControllerTest.class
            .getCanonicalName() + new Date().getTime();

    @BeforeClass
    public void setUp() throws IOException {
        Properties props = new Properties();

        InputStream is = new FileInputStream(new File(
                "src/test/resources/testapp.properties"));

        props.load(is);

        String uri = "hdfs://" + props.getProperty("hdfs.host") + ":"
                + props.getProperty("hdfs.port");

        this.hdfsService = new NativeHdfsService(uri);
        this.hdfsService.mkdirs(testDirectory);
        this.hdfsService.createNewFile(testDirectory+"/hello.1");
        this.hdfsService.createNewFile(testDirectory+"/hello.2");
        this.hdfsService.createNewFile(testDirectory+"/hello.3");
        
        
        System.setProperty("config", "src/test/resources/testapp.properties");

    }

    @AfterClass
    public void tearDown() throws IOException {
        this.hdfsService.delete(testDirectory, true);
    }

    @Test
    public void exists() throws FileNotFoundException, IOException {

        WebHdfsController controller = new WebHdfsController();

        Response resp = controller.exists(testDirectory);
        assertEquals("true", resp.getMessage());

        resp = controller.exists(testDirectory + ".1");
        assertEquals("false", resp.getMessage());
        
    }
    
    @Test
    public void list() throws FileNotFoundException, IOException {

        WebHdfsController controller = new WebHdfsController();
        Response resp = controller.list(testDirectory, true);
//        System.out.println(resp.get("RESULT").getClass());
        assertEquals(3, resp.getFiles().size());

        long start = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            controller.list(testDirectory, true);
        }

        System.out.println("50 calls took "
                + (System.currentTimeMillis() - start) + " ms");
    }

    @Test
    public void list_root() throws FileNotFoundException, IOException {
        WebHdfsController controller = new WebHdfsController();
        Response resp = controller.list("/", true);
//        System.out.println(resp.get("RESULT").getClass());
    }

    @Test
    public void listMultithreaded() throws FileNotFoundException, IOException, InterruptedException {

        WebHdfsController controller = new WebHdfsController();

        long start = System.currentTimeMillis();
        
        Set<ApiCaller> threads = new HashSet<ApiCaller>();
        
        for (int i=0; i<10;i++){
            ApiCaller t = new ApiCaller(controller, testDirectory);
            t.start();
            threads.add(t);
        }
        
        for (ApiCaller c : threads) {
            c.join();
        }
        
        System.out.println("10 thread/50 calls each took "
                + (System.currentTimeMillis() - start) + " ms");
    }
    
    class ApiCaller extends Thread {

        WebHdfsController controller = null;
        String path = null;

        public ApiCaller(WebHdfsController controller, String path) {
            this.controller = controller;
            this.path = path;
        }

        public void run() {
            try {
                for (int i = 0; i < 50; i++) {
                    controller.list(path, true);    
                }
            } catch (IOException e) {
                System.out.println(this.getName());
                e.printStackTrace();
            }
        }
    }
    
}
