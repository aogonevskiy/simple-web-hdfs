package com.arkadi4.hadoop.hdfs;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;

import com.arkadi4.hadoop.hdfs.model.FileInfo;

public class NativeHdfsService {

    private String uri = null;
    
    private Log log = LogFactory.getLog(NativeHdfsService.class);

    public NativeHdfsService(String uri) {
        super();
        this.uri = uri;
    }

    private FileSystem getHdfs() throws IOException {
        Configuration config = new Configuration();
        config.set("fs.default.name", uri);
        // config.setInt("dfs.replication", replicationFactor);
        return FileSystem.get(config);
    }

    public void listFiles(String path, List<FileInfo> files, boolean isRecursive)
            throws IOException {

        if (null == path) {
            throw new IllegalArgumentException("Path parameter can not be null");
        }

        FileSystem hdfs = getHdfs();

        try {

            if (!hdfs.exists(new Path(path))) {
                throw new IllegalArgumentException(
                        "Path does not exist. Path = " + path);
            }

            FileStatus[] fStatuses = hdfs.listStatus(new Path(path));
            
            

            for (FileStatus fs : fStatuses) {

                log.debug("*** Adding " + fs.getPath().getName() + " to list");

                files.add(FileInfo.fromFileStatus(fs, path + (path.endsWith("/")?"":"/")
                        + fs.getPath().getName(), fs.isDir()));

                if (fs.isDir() && isRecursive) {

                    String p = path + (path.endsWith("/") ? "" : "/")
                            + fs.getPath().getName();
                    
                    log.debug("******* Calling listFiles for " + p);

                    listFiles(p, files, isRecursive);

                }

            }
        } finally {
            // close() method closes cached filesystem. we do not need to do that.  
//            hdfs.close();
        }

    }
    
    /**
     * check if path exists
     */
    public boolean exists(String path) throws IOException {

        if (null == path) {
            throw new IllegalArgumentException("Path parameter can not be null");
        }

        FileSystem hdfs = getHdfs();
        boolean exists = hdfs.exists(new Path(path));

//        hdfs.close();

        return exists;
        
    }
    
    /**
     * check if path is a directory
     */
    public boolean isDir(String path) throws IOException {

        if (null == path) {
            throw new IllegalArgumentException("Path parameter can not be null");
        }

        FileSystem hdfs = getHdfs();
        
        if (!hdfs.exists(new Path(path))) {
            throw new IllegalArgumentException("Path does not exist. Path = "
                    + path);
        }

        boolean isDir = hdfs.getFileStatus(new Path(path)).isDir();

//        hdfs.close();

        return isDir;
        
    }
    
    /**
     * rename path
     */
    public boolean rename(String srcPath, String destPath) throws IOException {

        if (null == srcPath) {
            throw new IllegalArgumentException(
                    "Source path parameter can not be null");
        }

        if (null == destPath) {
            throw new IllegalArgumentException(
                    "Dest path parameter can not be null");
        }

        FileSystem hdfs = getHdfs();

        if (!hdfs.exists(new Path(srcPath))) {
            throw new IllegalArgumentException("Path does not exist. Path = "
                    + srcPath);
        }

        boolean success = hdfs.rename(new Path(srcPath), new Path(destPath));

//        hdfs.close();

        return success;

    }
    
    /**
     * create new file
     */
    public boolean createNewFile(String path) throws IOException {

        if (null == path) {
            throw new IllegalArgumentException("Path parameter can not be null");
        }

        FileSystem hdfs = getHdfs();

        boolean success = hdfs.createNewFile(new Path(path));

//        hdfs.close();

        return success;

    }

    /**
     * create new file
     */
    public boolean mkdirs(String path) throws IOException {

        if (null == path) {
            throw new IllegalArgumentException("Path parameter can not be null");
        }

        FileSystem hdfs = getHdfs();

        boolean success = hdfs.mkdirs(new Path(path));

//        hdfs.close();

        return success;

    }

    /**
     * delete path
     */
    public boolean delete(String path, boolean isRecursive) throws IOException {

        if (null == path) {
            throw new IllegalArgumentException("Path parameter can not be null");
        }

        FileSystem hdfs = getHdfs();

        boolean success = hdfs.delete(new Path(path), isRecursive);

//        hdfs.close();

        return success;

    }
    
    /**
     * check if path is an empty file
     */
    public boolean isEmptyFile(String path) throws IOException {

        if (null == path) {
            throw new IllegalArgumentException("Path parameter can not be null");
        }

        FileSystem hdfs = getHdfs();

        FileStatus fStatus = hdfs.getFileStatus(new Path(path));

        boolean isEmpty = false;
        if (null != fStatus) {
            isEmpty = (!fStatus.isDir() && 0 == fStatus.getLen());
        }

//        hdfs.close();

        return isEmpty;

    }
    
    public String copyToLocal(String hdfsPath, String localPath) throws IOException {

        if (null == hdfsPath) {
            throw new IllegalArgumentException("Path parameter can not be null");
        }

        FileSystem hdfs = getHdfs();
        
        if (!hdfs.exists(new Path(hdfsPath))) {
            throw new IllegalArgumentException("Path does not exist. Path = "
                    + hdfsPath);
        }
        
        hdfs.copyToLocalFile(new Path(hdfsPath), new Path(localPath));

//        hdfs.close();
        
        return localPath;

    }    

    public String copyFromLocal(String localPath, String hdfsPath)
            throws IOException {

        if (null == localPath) {
            throw new IllegalArgumentException(
                    "localPath parameter can not be null");
        }

        if (null == hdfsPath) {
            throw new IllegalArgumentException(
                    "hdfsPath parameter can not be null");
        }

        File f = new File(localPath);
        if (!f.exists()) {
            throw new IllegalArgumentException(
                    "Local file does not exist. path = " + localPath);
        }
        if (f.isDirectory()) {
            throw new IllegalArgumentException(
                    "Local path is a directory. Directory copying is not supported. Path = "
                            + localPath);
        }

        FileSystem hdfs = getHdfs();

        hdfs.copyFromLocalFile(new Path(localPath), new Path(hdfsPath));

//        hdfs.close();

        return localPath;

    }

}
