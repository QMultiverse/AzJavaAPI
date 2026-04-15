package api.acceptancetests.tasks;

import api.acceptancetests.endpoints.DummyJsonEndPoints;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Get;

public class FetchUsers {

    public static Performable all() {
        return Task.where("{0} fetches all users",
                Get.resource(DummyJsonEndPoints.USERS)
        );
    }

    public static Performable withId(int id) {
        return Task.where("{0} fetches user with ID " + id,
                Get.resource(DummyJsonEndPoints.USER_BY_ID)
                        .with(request -> request.pathParam("id", id))
        );
    }

    public static Performable searchFor(String query) {
        return Task.where("{0} searches for users matching '" + query + "'",
                Get.resource(DummyJsonEndPoints.SEARCH_USERS)
                        .with(request -> request.queryParam("q", query))
        );
    }
}
