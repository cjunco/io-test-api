package runner;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunnerTest {

    @Test
    void testParallel() {
        String karateTag = System.getProperty("karate.tag");

        Results results = Runner.path("classpath:features")
                .tags(karateTag)
                .parallel(5);
        Assertions.assertEquals(0, results.getFailCount(), results.getErrorMessages());

        deletePreviousReports(results.getReportDir());

        generateReport(results.getReportDir());
    }

    private static void deletePreviousReports(String karateOutputPath) {
        try {
            Files.walk(Paths.get(karateOutputPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith("karate-summary"))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateReport(String karateOutputPath) {
        try {
            File file = new File(karateOutputPath + "/karate-summary.html");
            if (file.exists()) {
                System.out.println("Karate summary report generated:");
                Files.lines(Paths.get(file.getAbsolutePath())).forEach(System.out::println);
            } else {
                System.out.println("Karate summary report not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
