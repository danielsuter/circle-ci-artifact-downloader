package ch.danielsuter.circleci;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Workflows {
    public String job_name;
    public String workflow_name;
    public String job_id;
}
