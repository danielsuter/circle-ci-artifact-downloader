package ch.danielsuter.circleci;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Artifact {
    public String path;
    public String url;
}
