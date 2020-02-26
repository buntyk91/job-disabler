package job_disabler;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by bunty.kumar on 2/20/18.
 */
@Slf4j
public class KillCoordinator {

    JobConfig jobConfig;

    public KillCoordinator(JobConfig jobConfig) {
        this.jobConfig = jobConfig;
    }

    public List<String> getJobURL(Map hashMap) {

        List<String> ooziePostURLList = new ArrayList<>();

        //Connection to proxy
        System.setProperty("http.proxyHost", jobConfig.getProxyHost());
        System.setProperty("http.proxyPort", jobConfig.getPort());

        Iterator iterator = hashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            log.info(pair.getKey() + " = " + pair.getValue());
            String coordinatorId = pair.getKey().toString();
            String finalCoordinatorID = coordinatorId.substring(1, coordinatorId.length() - 1);
            String username = pair.getValue().toString();
            String ooziePostURL = jobConfig.getOozieURL() + finalCoordinatorID + jobConfig.getActionName() + username;
            log.info(ooziePostURL);
            ooziePostURLList.add(ooziePostURL);
            log.info(ooziePostURL);
        }
        return ooziePostURLList;
    }

    public void killCoordinator(List<String> ooziePostURLList) throws IOException {

        int killedCoordinators = 0;
        for (String urls : ooziePostURLList) {
            URL url = new URL(urls);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            log.info("success");
            killedCoordinators = killedCoordinators + 1;

            connection.setRequestMethod("PUT");
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
            }
        }
        log.info("The total number of coordinators killed : {}", killedCoordinators);
    }

    public void run(Map<List<String>, String> killHashMap) throws IOException {
        killCoordinator(getJobURL(killHashMap));
    }

}


