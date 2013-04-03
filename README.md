## DETAILS

simple-web-hdfs is a proxy on top of Hadoop HDFS. It is intended to be used with older versions of HDFS (0.20.2 for the moment)

## BUILDING

simple-web-hdfs is built with maven (http://maven.apache.org/). To build from the command line:
  
    mvn clean install

## INSTALLATION


## USAGE

### List files

Sample request:

    http://localhost:8080/simple-web-hdfs/list?path=/path/to/directory&is_recursive=true

Sample response:

    { "files" : [ 
        { "directory" : true,
          "modificationTime" : 1344642286054,
          "path" : "/hbase/-ROOT-"
        },
        { "directory" : true,
          "modificationTime" : 1344552270982,
          "path" : "/hbase/.META."
        },
        { "directory" : true,
          "modificationTime" : 1344555875027,
          "path" : "/hbase/.logs"
        },
        { "directory" : false,
          "modificationTime" : 1344552270685,
          "path" : "/hbase/hbase.version"
        }
    ],
    "statusCode" : 200
    }

### Check if file exists

Sample request:

    http://localhost:8080/simple-web-hdfs/exists?path=/path/to/file

### Check if path is a directory

Sample request:

    http://localhost:8080/simple-web-hdfs/isdir?path=/path/to/file

### Download

Sample request:

    http://localhost:8080/simple-web-hdfs/isdir?path=/path/to/file


