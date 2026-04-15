package api.acceptancetests.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import api.acceptancetests.questions.TheResponseCode;
import api.acceptancetests.questions.TheUserProfile;
import api.acceptancetests.tasks.FetchProfile;
import api.acceptancetests.tasks.LoginWith;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static org.hamcrest.Matchers.*;

public class AuthStepDefinitions {

    @When("a user logs in with username {string} and password {string}")
    public void loginWithCredentials(String username, String password) {
        Actor theUser = OnStage.theActorCalled("API User");
        theUser.attemptsTo(
                LoginWith.credentials(username, password)
        );
    }

    @Then("the login should be successful")
    public void theLoginShouldBeSuccessful() {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheResponseCode.returned(), equalTo(200))
        );
    }

    @Then("the response should include an access token")
    public void theResponseShouldIncludeAnAccessToken() {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheUserProfile.accessToken(), is(notNullValue()))
        );
    }

    @Then("the response should include user details for {string}")
    public void theResponseShouldIncludeUserDetailsFor(String username) {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheUserProfile.username(), equalTo(username))
        );
    }

    @Then("the login should fail with status code {int}")
    public void theLoginShouldFailWithStatusCode(int statusCode) {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheResponseCode.returned(), equalTo(statusCode))
        );
    }

    @Given("a user is logged in with username {string} and password {string}")
    public void aUserIsLoggedIn(String username, String password) {
        Actor theUser = OnStage.theActorCalled("API User");
        theUser.attemptsTo(
                LoginWith.credentialsAndRememberToken(username, password)
        );
    }

    @When("the user requests their profile")
    public void theUserRequestsTheirProfile() {
        OnStage.theActorInTheSpotlight().attemptsTo(
                FetchProfile.withToken()
        );
    }

    @Then("the profile should contain the username {string}")
    public void theProfileShouldContainTheUsername(String username) {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheResponseCode.returned(), equalTo(200)),
                seeThat(TheUserProfile.username(), equalTo(username))
        );
    }
}
