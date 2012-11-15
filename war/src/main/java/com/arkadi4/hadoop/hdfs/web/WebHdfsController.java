package com.arkadi4.hadoop.hdfs.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.arkadi4.hadoop.hdfs.NativeHdfsService;
import com.arkadi4.hadoop.hdfs.model.FileInfo;

@Controller
public class WebHdfsController {
	
	private Log log = LogFactory.getLog(WebHdfsController.class);
	
	private NativeHdfsService hdfsService = null;
	
	private String tmpDir = null;
	
	public WebHdfsController() throws FileNotFoundException, IOException {
		
	    log.info("########### WebHdfsController constructor ##########");

		String configFile = System.getProperty("config", "jetty.properties");
		System.getProperties().load(new FileInputStream(configFile));

		String hdfsHost = System.getProperty("hdfs.host");
		String hdfsPort = System.getProperty("hdfs.port");
		
		this.tmpDir = System.getProperty("temp.dir");
		
		String hdfsUri = "hdfs://" + hdfsHost + ":" + hdfsPort;
		
		log.info("Initializing HDFS URI = " + hdfsUri);
		
		hdfsService = new NativeHdfsService(hdfsUri);
	}

	// http://localhost:8080/simple-web-hdfs/list?path=/mfs/&is_recursive=true
    @RequestMapping("/list")
    public @ResponseBody
    Response list(
            @RequestParam(value = "path", required = false) String path,
            @RequestParam(value = "is_recursive", required = false) boolean isRecursive)
            throws IOException {

        Response resp = new Response(200);

        List<FileInfo> files = new ArrayList<FileInfo>();
        
        this.hdfsService.listFiles(path, files, isRecursive);

        resp.setFiles(files);

        return resp;

    }
    
    // http://localhost:8080/simple-web-hdfs/exists?path=/mfs/state_files/rex_ci_back_office/rex_data_repeater_jobs
    @RequestMapping("/exists")
    public @ResponseBody
    Response exists(@RequestParam(value = "path", required = false) String path)
            throws IOException {

        if (null == path) {
            throw new IllegalArgumentException(
                    "'path' attribute can not be null");
        }
        
        Response resp = new Response(200);
        resp.setMessage(String.valueOf(this.hdfsService.exists(path)));
        return resp;
        
    }
    
    // curl "http://localhost:8080/simple-web-hdfs/isdir?path=/1/start-dfs.sh"
    @RequestMapping("/isdir") 
    public @ResponseBody
    Response isdir(@RequestParam(value = "path", required = false) String path)
            throws IOException {

        if (null == path) {
            throw new IllegalArgumentException(
                    "'path' attribute can not be null");
        }

        Response resp = new Response(200);
        resp.setMessage(String.valueOf(this.hdfsService.isDir(path)));
        return resp;

    }
    

    @RequestMapping("/download")
    public @ResponseBody
    Response download(
            @RequestParam(value = "path", required = false) String path,
            HttpServletResponse resp) throws IOException {

        if (this.hdfsService.isDir(path)) {
            throw new IllegalArgumentException(
                    "Path ["
                            + path
                            + "] is a directory. Directory downloading is not supported.");
        }

        String fileName = getFileName(path);

        String localPath = this.tmpDir + File.separator + fileName;

        log.info("Downloading file into a local path = " + localPath);

        this.hdfsService.copyToLocal(path, localPath);

        File file = new File(localPath);

        resp.setHeader("Content-Disposition",
                "attachment; filename=\"" + file.getName() + "\"");

        OutputStream out = resp.getOutputStream();
        FileInputStream fi = new FileInputStream(file);
        IOUtils.copy(fi, out);
        out.flush();
        out.close();
        
        file.delete();
        
        return null;

    }
    
    // http://localhost:8080/simple-web-hdfs/delete?path=/tmp/hello.1&is_recursive=true
    // TODO secret key
    // disabling 'white' methods
//    @RequestMapping("/delete")
    public @ResponseBody
    Response delete(
            @RequestParam(value = "path", required = false) String path,
            @RequestParam(value = "is_recursive", required = false) boolean isRecursive)
            throws IOException {

        Response resp = new Response(200);

        boolean success = this.hdfsService.delete(path, isRecursive);

        resp.setMessage(String.valueOf(success));

        return resp;

    }

    // curl -F "file=@settings.xml" http://localhost:8080/simple-web-hdfs/upload?path=/tmp/
    // disabling 'white' methods
    //    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public @ResponseBody
    Response upload(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "path", required = false) String path)
            throws IOException {

        InputStream inputStream = null;
        OutputStream outputStream = null;
        
        if (null == path) {
            throw new IllegalArgumentException("path parameter can not be null");
        }
        
        String destinationPath = path;
        
        // Both clauses do the same. I know that.
        if (path.endsWith("/")) {
            // dest path is a directory. going to get real file name and
            // substitute
            destinationPath = path + "/" + file.getOriginalFilename();
        } else if (this.hdfsService.exists(path) && this.hdfsService.isDir(path)) {
            // already exists and directory
            destinationPath = path + "/" + file.getOriginalFilename();
            
        } else {
            
            // path does not end with / and does not exist
            // can not decide if it is a file or directory
            
        }
        
        String fileName = getFileName(destinationPath);

        if (file.getSize() > 0) {
            
            inputStream = file.getInputStream();

            log.info("size::" + file.getSize());

            String outputPath = tmpDir + File.separator + fileName;
            outputStream = new FileOutputStream(outputPath);

            log.info("fileName:" + fileName);

            int readBytes = 0;
            byte[] buffer = new byte[10000];
            while ((readBytes = inputStream.read(buffer, 0, 10000)) != -1) {
                outputStream.write(buffer, 0, readBytes);
            }
            
            outputStream.close();
            inputStream.close();
            
            this.hdfsService.copyFromLocal(outputPath, destinationPath);
            
            File f = new File(outputPath);
            f.delete();
            
        }
        
        Response resp = new Response(200);
        resp.setMessage("SUCCESS");
        return resp;

    }
    
    private String getFileName(String path) {
        return path.substring(path.lastIndexOf("/")+1);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public void handleException(final Exception e,
            final HttpServletRequest request, Writer writer) throws IOException {
        Response resp = new Response(500);
        resp.setMessage(e.getMessage());
        resp.setErrorClass(e.getClass().getCanonicalName());
//        return resp; 
//        writer.write(String.format(
//                "{\"error\":{\"java.class\":\"%s\", \"message\":\"%s\"}}\n",
//                e.getClass(), e.getMessage()));
        
        ObjectMapper mapper = new ObjectMapper();
        
        // This makes response readable.  
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        
        writer.write(mapper.writeValueAsString(resp));

    }

}
