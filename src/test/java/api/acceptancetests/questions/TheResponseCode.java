package api.acceptancetests.questions;

import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.rest.questions.TheResponse;

public class TheResponseCode {

    public static Question<Integer> returned() {
        return TheResponse.statusCode();
    }
}
