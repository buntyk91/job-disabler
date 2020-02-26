package job_disabler;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by bunty.kumar on 2/14/18.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OozieData {
    @JsonProperty("user")
    private String userName;

    @JsonProperty("coordJobId")
    private String coordinatorID;

}
