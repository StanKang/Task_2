package praktikum.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import praktikum.steps.OrderSteps;
import praktikum.steps.UserSteps;
import praktikum.utils.UserGenerator;

import java.util.List;

@DisplayName("Создание заказа")
class CreateOrderTest {

    private static final int INGREDIENTS_COUNT = 2;
    private static final String INVALID_INGREDIENT_HASH = "invalidIngredientHash";
    private static final String NO_INGREDIENTS_MESSAGE = "Ingredient ids must be provided";
    private static final String SERVER_ERROR_TEXT = "Internal Server Error";

    private final UserSteps userSteps = new UserSteps();
    private final OrderSteps orderSteps = new OrderSteps();
    private String token;
    private List<String> ingredients;

    @BeforeEach
    void setUp() {
        token = userSteps.getAccessToken(userSteps.createUser(UserGenerator.generateUser()));
        ingredients = orderSteps.getIngredientIds(orderSteps.getIngredients(), INGREDIENTS_COUNT);
    }

    @AfterEach
    void tearDown() {
        if (token != null) {
            userSteps.deleteUser(token);
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами возвращает номер заказа")
    void createOrderWithAuthAndIngredientsReturnsOrderNumber() {
        Response response = orderSteps.createOrder(token, ingredients);

        orderSteps.checkStatusCode(response, 200);
        orderSteps.checkSuccess(response, true);
        orderSteps.checkOrderCreated(response);
    }

    @Test
    @DisplayName("Создание заказа без авторизации с ингредиентами возвращает номер заказа")
    void createOrderWithoutAuthAndWithIngredientsReturnsOrderNumber() {
        Response response = orderSteps.createOrderWithoutAuth(ingredients);

        orderSteps.checkStatusCode(response, 200);
        orderSteps.checkSuccess(response, true);
        orderSteps.checkOrderCreated(response);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Создание заказа без ингредиентов возвращает ошибку")
    void createOrderWithoutIngredientsReturnsBadRequest(List<String> emptyIngredients) {
        Response response = orderSteps.createOrder(token, emptyIngredients);

        orderSteps.checkStatusCode(response, 400);
        orderSteps.checkSuccess(response, false);
        orderSteps.checkMessage(response, NO_INGREDIENTS_MESSAGE);
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиента возвращает ошибку сервера")
    void createOrderWithInvalidIngredientHashReturnsServerError() {
        Response response = orderSteps.createOrder(token, List.of(INVALID_INGREDIENT_HASH));

        orderSteps.checkStatusCode(response, 500);
        orderSteps.checkBodyContains(response, SERVER_ERROR_TEXT);
    }
}
