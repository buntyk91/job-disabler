package job_disabler;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Created by bunty.kumar on 9/21/18.
 */


/**
 * Get everything from file to String, Works with DFS
 *
 * @return
 * @throws IOException
 */

@Slf4j
public class Util {

    public static String getFromLocalFile(String filePath, Configuration conf) throws IOException {

        //Get the filesystem - HDFS
        FileSystem fs = FileSystem.get(URI.create(filePath), conf);
        FSDataInputStream fsDataInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            //Open the path mentioned in HDFS
            fsDataInputStream = fs.open(new Path(URI.create(filePath)));
            IOUtils.copyBytes(fsDataInputStream, byteArrayOutputStream, 4096, false);

        } finally {
            IOUtils.closeStream(fsDataInputStream);
        }
        return new String(byteArrayOutputStream.toByteArray());
    }
}
