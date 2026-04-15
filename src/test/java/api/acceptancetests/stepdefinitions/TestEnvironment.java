package api.acceptancetests.stepdefinitions;

import net.thucydides.model.util.EnvironmentVariables;

public class TestEnvironment {

    private final EnvironmentVariables environmentVariables;

    public TestEnvironment(EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public String getBaseUrl() {
        return environmentVariables.optionalProperty("restapi.baseurl")
                                   .orElse("https://dummyjson.com");
    }
}
