package job_disabler;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


/**
 * Created by bunty.kumar on 2/3/18.
 */

/*OozieConnection is the class which will connect to the oozie URL and will fetch the
* currently running schedules for all the users with the user names.*/

@Slf4j
public class OozieConnection {

    JobConfig jobConfig;

    public OozieConnection(JobConfig jobConfig) {
        this.jobConfig = jobConfig;
    }

    public List<OozieData> connectToOozie() throws IOException {
        List<OozieData> oozieJobsList = null;

        try {
            System.setProperty("http.proxyHost", jobConfig.getProxyHost());
            System.setProperty("http.proxyPort", jobConfig.getPort());

            URL url = new URL(jobConfig.getOozieGetURL());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(connection.getInputStream());
            JsonNode jn = jsonNode.get("coordinatorjobs");
            ObjectReader objectReader = mapper.reader(new TypeReference<List<OozieData>>() {
            });

            oozieJobsList = objectReader.readValue(jn);
            log.info("Total number of jobs found :{}", String.valueOf(oozieJobsList.size()));
            connection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return oozieJobsList;
    }
}
