package ch.danielsuter.circleci;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class CircleCiApi {

    private final String token;
    private final String vcs;
    private final String user;
    private final String project;
    private final String branch;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public CircleCiApi(String token, String vcs, String user, String project, String branch) {
        this.token = token;
        this.vcs = vcs;
        this.user = user;
        this.project = project;
        this.branch = branch;

        // Support for Java 8 dates
        objectMapper.registerModule(new JavaTimeModule());
    }

    public List<Artifact> getArtifacts(int buildNumber) {
        HttpUrl url = HttpUrl.parse("https://circleci.com/api/v1.1/project")
                .newBuilder()
                .addPathSegment(vcs)
                .addPathSegment(user)
                .addPathSegment(project)
                .addPathSegment("" + buildNumber)
                .addPathSegment("artifacts")
                .addQueryParameter("circle-token", token)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            String source = response.body().string();
            return objectMapper.readValue(source, new TypeReference<List<Artifact>>() {
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<Build> getRecentBuilds(int from, int limit, String filter) {
        HttpUrl url = getBuilder().addQueryParameter("limit", "" + limit)
                .addQueryParameter("from", "" + from)
                .addQueryParameter("filter", filter)
                .addQueryParameter("shallow", "true")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            String source = response.body().string();
            return objectMapper.readValue(source, new TypeReference<List<Build>>() {
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private HttpUrl.Builder getBuilder() {
        return HttpUrl.parse("https://circleci.com/api/v1.1/project")
                .newBuilder()
                .addPathSegment(vcs)
                .addPathSegment(user)
                .addPathSegment(project)
                .addPathSegment("tree")
                .addPathSegment(branch)
                .addQueryParameter("circle-token", token);
    }

    public void downloadArtifact(Artifact artifact, String targetPath) {
        Request request = new Request.Builder().url(artifact.url + "?circle-token=" + token).build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            File targetFile = new File(targetPath, artifact.path);
            FileOutputStream fos = new FileOutputStream(targetFile);
            fos.write(response.body().bytes());
            fos.close();
            System.out.println("Downloaded file to " + targetFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
