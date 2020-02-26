package job_disabler;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bunty.kumar on 2/20/18.
 */
/*This class will compare the total running coordinators of the users with the users who are terminated*/
@Slf4j
public class CompareLists {
    Map<List<String>, String> coordinators = new HashMap<>();

    public Map<List<String>, String> compareLists(ArrayList<String> LDAPList, List<OozieData> oozieDataList) {
        log.info("Below are the terminated users and their running coordinators");
        for (OozieData user : oozieDataList) {
            String userName = user.getUserName();
            List<String> coordinatorId = new ArrayList<>();
            coordinatorId.add(user.getCoordinatorID());

            if (LDAPList.contains((userName))) {
                coordinators.put(coordinatorId, userName);
                log.info("user: " + userName + ", coordinatorId: " + coordinatorId);
            } else {
                log.debug("Not Found user::{} coordinatorId::{}", userName, coordinatorId);
            }
        }
        return coordinators;
    }
}
