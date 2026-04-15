package api.acceptancetests.tasks;

import api.acceptancetests.endpoints.DummyJsonEndPoints;
import net.bddtrader.model.User;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;

public class CreateUser {

    public static Performable withName(String firstName, String lastName) {
        User user = new User(firstName, lastName, 25);
        return Task.where("{0} creates a new user " + firstName + " " + lastName,
                Post.to(DummyJsonEndPoints.ADD_USER)
                        .with(request -> request
                                .header("Content-Type", "application/json")
                                .body(user)
                        )
        );
    }
}
