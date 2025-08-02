package org.mangorage.mavenchecker;

import com.reposilite.Reposilite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class Runner {
    public static void main(String[] args) throws IOException {
        Path from = Path.of("F:\\Downloads\\MavenChecker\\build\\libs\\MavenChecker-1.0-SNAPSHOT.jar");
        Path to = Path.of("F:\\Downloads\\MavenChecker\\repo\\plugins\\MavenChecker.jar");

        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);

        // Should be good to go!

        Reposilite a = com.reposilite.ReposiliteLauncherKt
                .createWithParameters(
                        "--working-directory", "repo"
                ).launch().get();
    }
}
