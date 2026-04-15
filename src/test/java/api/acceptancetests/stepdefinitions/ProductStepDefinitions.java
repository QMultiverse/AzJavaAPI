package api.acceptancetests.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import api.acceptancetests.questions.TheProducts;
import api.acceptancetests.questions.TheResponseCode;
import api.acceptancetests.questions.TheUserProfile;
import api.acceptancetests.tasks.FetchProducts;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;

import java.util.List;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

public class ProductStepDefinitions {

    @When("a user requests the list of products")
    public void requestListOfProducts() {
        Actor theUser = OnStage.theActorCalled("API User");
        theUser.attemptsTo(FetchProducts.all());
    }

    @Then("the response should contain a list of products")
    public void theResponseShouldContainAListOfProducts() {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheResponseCode.returned(), equalTo(200)),
                seeThat(TheProducts.count(), greaterThan(0))
        );
    }

    @Then("the total number of products should be greater than {int}")
    public void theTotalNumberOfProductsShouldBeGreaterThan(int count) {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheProducts.total(), greaterThan(count))
        );
    }

    @When("a user requests the product with ID {int}")
    public void requestProductById(int id) {
        Actor theUser = OnStage.theActorCalled("API User");
        theUser.attemptsTo(FetchProducts.withId(id));
    }

    @Then("the response should contain product details")
    public void theResponseShouldContainProductDetails() {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheResponseCode.returned(), equalTo(200)),
                seeThat(TheUserProfile.userId(), greaterThan(0))
        );
    }

    @Then("the product should have a title")
    public void theProductShouldHaveATitle() {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheProducts.title(), is(notNullValue()))
        );
    }

    @Then("the product should have a price greater than {int}")
    public void theProductShouldHaveAPriceGreaterThan(int price) {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheProducts.price(), greaterThan((double) price))
        );
    }

    @When("a user searches for products with the query {string}")
    public void searchForProducts(String query) {
        Actor theUser = OnStage.theActorCalled("API User");
        theUser.attemptsTo(FetchProducts.searchFor(query));
    }

    @Then("all returned products should be related to {string}")
    public void allReturnedProductsShouldBeRelatedTo(String keyword) {
        OnStage.theActorInTheSpotlight().should(
                seeThat(TheProducts.count(), greaterThan(0))
        );
        List<String> titles = OnStage.theActorInTheSpotlight().asksFor(TheProducts.titles());
        String lowerKeyword = keyword.toLowerCase();
        assertThat(titles).anyMatch(title -> title.toLowerCase().contains(lowerKeyword));
    }

    @When("a user browses products in the {string} category")
    public void browseProductsByCategory(String category) {
        Actor theUser = OnStage.theActorCalled("API User");
        theUser.attemptsTo(FetchProducts.inCategory(category));
    }

    @Then("all returned products should be in the {string} category")
    public void allReturnedProductsShouldBeInCategory(String category) {
        List<String> categories = OnStage.theActorInTheSpotlight().asksFor(TheProducts.categories());
        assertThat(categories).allMatch(cat -> cat.equalsIgnoreCase(category));
    }
}
