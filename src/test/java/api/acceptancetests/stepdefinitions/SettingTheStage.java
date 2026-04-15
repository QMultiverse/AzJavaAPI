package api.acceptancetests.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import api.acceptancetests.actors.CastOfApiUsers;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;
import net.thucydides.model.util.EnvironmentVariables;

import static net.serenitybdd.screenplay.actors.OnStage.setTheStage;

public class SettingTheStage {

    EnvironmentVariables environmentVariables;

    @Before
    public void set_the_stage() {
        setTheStage(new CastOfApiUsers(environmentVariables));
    }

    @ParameterType(".*")
    public Actor actor(String name) {
        return OnStage.theActorCalled(name);
    }
}
