package api.acceptancetests.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Question;

import java.util.List;

public class TheProducts {

    public static Question<List<String>> titles() {
        return Question.about("the product titles")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getList("products.title"));
    }

    public static Question<List<String>> categories() {
        return Question.about("the product categories")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getList("products.category"));
    }

    public static Question<Integer> total() {
        return Question.about("the total number of products")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getInt("total"));
    }

    public static Question<Integer> count() {
        return Question.about("the number of products returned")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getList("products").size());
    }

    public static Question<String> title() {
        return Question.about("the product title")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getString("title"));
    }

    public static Question<Double> price() {
        return Question.about("the product price")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getDouble("price"));
    }
}
