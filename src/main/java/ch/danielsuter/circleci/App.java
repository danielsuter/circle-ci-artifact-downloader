package ch.danielsuter.circleci;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    private final static String targetPath = "C:\\Users\\suter\\Google Drive\\Shinsei Kan Lenzburg\\Website\\CRM";

    public static void main(String[] args) {
        String token = "TODO";

        CircleCiApi api = new CircleCiApi(token, "bitbucket", "Sanix", "shinseikan-crm", "develop");

        List<Build> builds = api.getRecentBuilds(0, 100, "succesful");
        List<Build> backups = builds.stream()
                .filter(build -> build.workflows.workflowName.equals("database_backup"))
                .filter(build -> build.startTime.isAfter(LocalDate.of(2019, 8, 9)))
                .collect(Collectors.toList());
        System.out.println("Number of backups: " + backups.size());

        List<Artifact> artifacts = backups.parallelStream()
                .map(backup -> backup.buildNumber)
                .map(buildNumber -> api.getArtifacts(buildNumber).get(0))
                .collect(Collectors.toList());
        System.out.println("Number of urls: " + artifacts.size());

        artifacts.stream()
                .filter(App::notExists)
                .forEach(artifact -> download(api, artifact));

    }

    private static void download(CircleCiApi api, Artifact artifact) {
        api.downloadArtifact(artifact, targetPath);
    }

    private static boolean notExists(Artifact artifact) {
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
