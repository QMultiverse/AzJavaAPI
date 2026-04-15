package api.acceptancetests.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import api.acceptancetests.questions.TheResponseCode;
import api.acceptancetests.questions.TheUserProfile;
import api.acceptancetests.questions.TheUsers;
import api.acceptancetests.tasks.CreateUser;
import api.acceptancetests.tasks.FetchUsers;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;

import java.util.List;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static org.hamcrest.Matchers.*;

public class UserStepDefinitions {

    @When("a user requests the list of users")
    public void requestListOfUsers() {
        Actor theUser = OnStage.theActorCalled("API User");
        theUser.attemptsTo(FetchUsers.all());
    }

    @Then("the response should contain a list of users")
    public void theResponseShouldContainAListOfUsers() {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheResponseCode.returned(), equalTo(200)),
                seeThat(TheUsers.count(), greaterThan(0))
        );
    }

    @Then("the total number of users should be greater than {int}")
    public void theTotalNumberOfUsersShouldBeGreaterThan(int count) {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheUsers.total(), greaterThan(count))
        );
    }

    @When("a user requests the user with ID {int}")
    public void requestUserById(int id) {
        Actor theUser = OnStage.theActorCalled("API User");
        theUser.attemptsTo(FetchUsers.withId(id));
    }

    @Then("the response should contain user details")
    public void theResponseShouldContainUserDetails() {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheResponseCode.returned(), equalTo(200)),
                seeThat(TheUserProfile.userId(), greaterThan(0))
        );
    }

    @Then("the user's first name should be {string}")
    public void theUsersFirstNameShouldBe(String firstName) {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheUserProfile.firstName(), equalTo(firstName))
        );
    }

    @Then("the user's last name should be {string}")
    public void theUsersLastNameShouldBe(String lastName) {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheUserProfile.lastName(), equalTo(lastName))
        );
    }

    @When("a user searches for users with the query {string}")
    public void searchForUsers(String query) {
        Actor theUser = OnStage.theActorCalled("API User");
        theUser.attemptsTo(FetchUsers.searchFor(query));
    }

    @Then("the results should contain a user with last name {string}")
    public void theResultsShouldContainAUserWithLastName(String lastName) {
        List<String> lastNames = OnStage.theActorInTheSpotlight().asksFor(TheUsers.lastNames());
        org.assertj.core.api.Assertions.assertThat(lastNames).anyMatch(name -> name.contains(lastName));
    }

    @When("a user creates a new user with first name {string} and last name {string}")
    public void createNewUser(String firstName, String lastName) {
        Actor theUser = OnStage.theActorCalled("API User");
        theUser.attemptsTo(CreateUser.withName(firstName, lastName));
    }

    @Then("the new user should be created successfully")
    public void theNewUserShouldBeCreatedSuccessfully() {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheResponseCode.returned(), is(anyOf(equalTo(200), equalTo(201))))
        );
    }

    @Then("the response should contain first name {string}")
    public void theResponseShouldContainFirstName(String firstName) {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheUserProfile.firstName(), equalTo(firstName))
        );
    }

    @Then("the response should contain last name {string}")
    public void theResponseShouldContainLastName(String lastName) {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheUserProfile.lastName(), equalTo(lastName))
        );
    }
}
