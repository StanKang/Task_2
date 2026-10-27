package praktikum.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import praktikum.steps.OrderSteps;
import praktikum.steps.UserSteps;
import praktikum.utils.UserGenerator;

import java.util.List;

@DisplayName("Получение заказов пользователя")
class GetUserOrdersTest {

    private static final int INGREDIENTS_COUNT = 2;
    private static final String UNAUTHORISED_MESSAGE = "You should be authorised";

    private final UserSteps userSteps = new UserSteps();
    private final OrderSteps orderSteps = new OrderSteps();
    private String token;
    private List<String> ingredients;
    private Integer orderNumber;

    @BeforeEach
    void setUp() {
        token = userSteps.getAccessToken(userSteps.createUser(UserGenerator.generateUser()));
        ingredients = orderSteps.getIngredientIds(orderSteps.getIngredients(), INGREDIENTS_COUNT);
        orderNumber = orderSteps.getOrderNumber(orderSteps.createOrder(token, ingredients));
    }

    @AfterEach
    void tearDown() {
        if (token != null) {
            userSteps.deleteUser(token);
        }
    }

    @Test
    @DisplayName("Получение заказов авторизованного пользователя возвращает его заказ")
    void getOrdersWithAuthReturnsUserOrder() {
        Response response = orderSteps.getUserOrders(token);

        orderSteps.checkStatusCode(response, 200);
        orderSteps.checkSuccess(response, true);
        orderSteps.checkUserOrders(response, orderNumber, ingredients);
        orderSteps.checkOrdersTotal(response);
    }

    @Test
    @DisplayName("Получение заказов неавторизованного пользователя возвращает ошибку")
    void getOrdersWithoutAuthReturnsUnauthorized() {
        Response response = orderSteps.getUserOrdersWithoutAuth();

        orderSteps.checkStatusCode(response, 401);
        orderSteps.checkSuccess(response, false);
        orderSteps.checkMessage(response, UNAUTHORISED_MESSAGE);
    }
}
