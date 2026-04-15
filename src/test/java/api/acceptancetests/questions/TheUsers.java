package api.acceptancetests.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Question;

import java.util.List;

public class TheUsers {

    public static Question<List<String>> lastNames() {
        return Question.about("the users last names")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getList("users.lastName"));
    }

    public static Question<Integer> total() {
        return Question.about("the total number of users")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getInt("total"));
    }

    public static Question<Integer> count() {
        return Question.about("the number of users returned")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getList("users").size());
    }
}
