package ch.danielsuter.circleci;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class App {

    public static void main(String[] args) {

        try (FileInputStream configStream = new FileInputStream("configuration.properties")) {

            Properties configuration = new Properties();
            configuration.load(configStream);

            String targetPath = configuration.getProperty("target_path");
            String token = configuration.getProperty("token");
            String vcs = configuration.getProperty("vcs");
            String user = configuration.getProperty("user");
            String project = configuration.getProperty("project");
            String branch = configuration.getProperty("branch");
            CircleCiApi api = new CircleCiApi(token, vcs, user, project, branch);

            System.out.println("Reading jobs...");
            List<Build> builds = api.getRecentBuilds(0, 100, "successful");
            System.out.println("Got " + builds.size() + " jobs");
            List<Build> backups = builds.stream()
                    .filter(build -> build.workflows.workflowName.equals("database_backup"))
                    .filter(build -> build.startTime.isAfter(LocalDate.of(2020, 4, 1)))
                    .collect(Collectors.toList());
            System.out.println("Number of backups: " + backups.size());

            List<Artifact> artifacts = backups.parallelStream()
                    .map(backup -> backup.buildNumber)
                    .map(buildNumber -> api.getArtifacts(buildNumber).get(0))
                    .collect(Collectors.toList());
            System.out.println("Number of urls: " + artifacts.size());

            artifacts.stream()
                    .filter(artifact -> notExists(artifact, targetPath))
                    .forEach(artifact -> download(api, artifact, targetPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void download(CircleCiApi api, Artifact artifact, String targetPath) {
        api.downloadArtifact(artifact, targetPath);
    }

    private static boolean notExists(Artifact artifact, String targetPath) {
        String cleanFileName = cleanFileName(artifact.path);

        return !new File(targetPath, cleanFileName).exists();
    }

    private static String cleanFileName(String path) {
        if (path.length() > 12) {
            String[] parts = path.split("\\.");
            return parts[0].substring(0, 8) + "." + parts[1];
        }
        return path;
    }
}
