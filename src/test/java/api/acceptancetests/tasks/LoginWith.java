package api.acceptancetests.tasks;

import api.acceptancetests.endpoints.DummyJsonEndPoints;
import net.bddtrader.model.LoginRequest;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;

public class LoginWith {

    public static Performable credentials(String username, String password) {
        return Task.where("{0} logs in as " + username,
                Post.to(DummyJsonEndPoints.LOGIN)
                        .with(request -> request
                                .header("Content-Type", "application/json")
                                .body(new LoginRequest(username, password))
                        )
        );
    }

    public static Performable credentialsAndRememberToken(String username, String password) {
        return Task.where("{0} logs in as " + username + " and remembers the token",
                actor -> {
                    actor.attemptsTo(
                            Post.to(DummyJsonEndPoints.LOGIN)
                                    .with(request -> request
                                            .header("Content-Type", "application/json")
                                            .body(new LoginRequest(username, password))
                                    )
                    );
                    if (SerenityRest.lastResponse().statusCode() == 200) {
                        String token = SerenityRest.lastResponse().jsonPath().getString("accessToken");
                        actor.remember("accessToken", token);
                    }
                }
        );
    }
}
