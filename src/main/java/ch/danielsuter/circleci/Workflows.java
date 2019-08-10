package ch.danielsuter.circleci;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Workflows {
    @JsonProperty("job_name")
    public String jobName;

    @JsonProperty("workflow_name")
    public String workflowName;

    @JsonProperty("job_id")
    public String jobId;
}
