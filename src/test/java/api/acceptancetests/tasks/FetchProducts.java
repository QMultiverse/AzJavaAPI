package api.acceptancetests.tasks;

import api.acceptancetests.endpoints.DummyJsonEndPoints;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Get;

public class FetchProducts {

    public static Performable all() {
        return Task.where("{0} fetches all products",
                Get.resource(DummyJsonEndPoints.PRODUCTS)
        );
    }

    public static Performable withId(int id) {
        return Task.where("{0} fetches product with ID " + id,
                Get.resource(DummyJsonEndPoints.PRODUCT_BY_ID)
                        .with(request -> request.pathParam("id", id))
        );
    }

    public static Performable searchFor(String query) {
        return Task.where("{0} searches for products matching '" + query + "'",
                Get.resource(DummyJsonEndPoints.SEARCH_PRODUCTS)
                        .with(request -> request.queryParam("q", query))
        );
    }

    public static Performable inCategory(String category) {
        return Task.where("{0} browses products in category '" + category + "'",
                Get.resource(DummyJsonEndPoints.PRODUCTS_BY_CATEGORY)
                        .with(request -> request.pathParam("category", category))
        );
    }
}
