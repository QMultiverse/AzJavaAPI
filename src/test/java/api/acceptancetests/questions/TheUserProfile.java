package api.acceptancetests.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Question;

public class TheUserProfile {

    public static Question<String> username() {
        return Question.about("the user profile username")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getString("username"));
    }

    public static Question<String> firstName() {
        return Question.about("the user profile first name")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getString("firstName"));
    }

    public static Question<String> lastName() {
        return Question.about("the user profile last name")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getString("lastName"));
    }

    public static Question<String> accessToken() {
        return Question.about("the user profile access token")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getString("accessToken"));
    }

    public static Question<Integer> userId() {
        return Question.about("the user profile ID")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getInt("id"));
    }
}
