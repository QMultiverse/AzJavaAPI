package api.acceptancetests.tasks;

import api.acceptancetests.endpoints.DummyJsonEndPoints;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Get;

public class FetchProfile {

    public static Performable withToken() {
        return Task.where("{0} fetches their profile",
                actor -> {
                    String token = actor.recall("accessToken");
                    actor.attemptsTo(
                            Get.resource(DummyJsonEndPoints.AUTH_ME)
                                    .with(request -> request
                                            .header("Authorization", "Bearer " + token)
                                    )
                    );
                }
        );
    }
}
