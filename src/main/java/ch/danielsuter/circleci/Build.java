package ch.danielsuter.circleci;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Build {
    public Workflows workflows;
    public int build_num;
}
