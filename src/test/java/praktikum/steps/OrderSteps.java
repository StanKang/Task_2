package praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import praktikum.models.Ingredient;
import praktikum.models.IngredientsResponse;
import praktikum.models.OrderRequest;
import praktikum.models.OrderResponse;
import praktikum.models.OrdersResponse;
import praktikum.models.UserOrder;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderSteps extends BaseSteps {

    private static final String INGREDIENTS_PATH = "/api/ingredients";
    private static final String ORDERS_PATH = "/api/orders";

    @Step("Получить список ингредиентов")
    public Response getIngredients() {
        return getSpec()
                .when()
                .get(INGREDIENTS_PATH);
    }

    @Step("Создать заказ с авторизацией")
    public Response createOrder(String token, List<String> ingredients) {
        return getSpec()
                .header("Authorization", token)
                .body(new OrderRequest(ingredients))
                .when()
                .post(ORDERS_PATH);
    }

    @Step("Создать заказ без авторизации")
    public Response createOrderWithoutAuth(List<String> ingredients) {
        return getSpec()
                .body(new OrderRequest(ingredients))
                .when()
                .post(ORDERS_PATH);
    }

    @Step("Получить заказы пользователя с авторизацией")
    public Response getUserOrders(String token) {
        return getSpec()
                .header("Authorization", token)
                .when()
                .get(ORDERS_PATH);
    }

    @Step("Получить заказы пользователя без авторизации")
    public Response getUserOrdersWithoutAuth() {
        return getSpec()
                .when()
                .get(ORDERS_PATH);
    }

    @Step("Получить идентификаторы {count} ингредиентов из ответа")
    public List<String> getIngredientIds(Response response, int count) {
        return response.as(IngredientsResponse.class).getData().stream()
                .limit(count)
                .map(Ingredient::getId)
                .collect(Collectors.toList());
    }

    @Step("Получить номер заказа из ответа")
    public Integer getOrderNumber(Response response) {
        return response.as(OrderResponse.class).getOrder().getNumber();
    }

    @Step("Проверить, что список ингредиентов не пуст")
    public void checkIngredientsReturned(Response response) {
        assertFalse(response.as(IngredientsResponse.class).getData().isEmpty());
    }

    @Step("Проверить, что заказ создан и ему присвоен номер")
    public void checkOrderCreated(Response response) {
        OrderResponse body = response.as(OrderResponse.class);

        assertNotNull(body.getName());
        assertFalse(body.getName().isEmpty());
        assertNotNull(body.getOrder());
        assertNotNull(body.getOrder().getNumber());
    }

    @Step("Проверить, что в списке заказов есть заказ с номером {expectedNumber} и теми же ингредиентами")
    public void checkUserOrders(Response response, Integer expectedNumber, List<String> expectedIngredients) {
        OrdersResponse body = response.as(OrdersResponse.class);

        assertNotNull(body.getOrders());
        assertFalse(body.getOrders().isEmpty());
        UserOrder order = body.getOrders().stream()
                .filter(userOrder -> expectedNumber.equals(userOrder.getNumber()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Заказ с номером " + expectedNumber + " не найден"));
        assertEquals(expectedIngredients, order.getIngredients());
        assertNotNull(order.getStatus());
    }

    @Step("Проверить, что в ответе указано общее количество заказов")
    public void checkOrdersTotal(Response response) {
        OrdersResponse body = response.as(OrdersResponse.class);

        assertNotNull(body.getTotal());
        assertTrue(body.getTotal() > 0);
        assertNotNull(body.getTotalToday());
    }
}
