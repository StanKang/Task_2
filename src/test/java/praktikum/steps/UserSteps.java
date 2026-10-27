package praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import praktikum.models.AuthResponse;
import praktikum.models.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserSteps extends BaseSteps {

    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String USER_PATH = "/api/auth/user";

    @Step("Создать пользователя")
    public Response createUser(User user) {
        return getSpec()
                .body(user)
                .when()
                .post(REGISTER_PATH);
    }

    @Step("Авторизовать пользователя")
    public Response loginUser(User user) {
        return getSpec()
                .body(user)
                .when()
                .post(LOGIN_PATH);
    }

    @Step("Изменить данные пользователя с авторизацией")
    public Response updateUser(String token, User user) {
        return getSpec()
                .header("Authorization", token)
                .body(user)
                .when()
                .patch(USER_PATH);
    }

    @Step("Изменить данные пользователя без авторизации")
    public Response updateUserWithoutAuth(User user) {
        return getSpec()
                .body(user)
                .when()
                .patch(USER_PATH);
    }

    @Step("Удалить пользователя")
    public Response deleteUser(String token) {
        return getSpec()
                .header("Authorization", token)
                .when()
                .delete(USER_PATH);
    }

    @Step("Получить токен доступа из ответа")
    public String getAccessToken(Response response) {
        return response.as(AuthResponse.class).getAccessToken();
    }

    @Step("Проверить, что в ответе есть токены доступа и обновления")
    public void checkTokensReturned(Response response) {
        AuthResponse body = response.as(AuthResponse.class);

        assertNotNull(body.getAccessToken());
        assertTrue(body.getAccessToken().startsWith("Bearer "));
        assertNotNull(body.getRefreshToken());
    }

    @Step("Проверить, что в ответе email: {expectedEmail}, имя: {expectedName}")
    public void checkUser(Response response, String expectedEmail, String expectedName) {
        AuthResponse body = response.as(AuthResponse.class);

        assertNotNull(body.getUser());
        assertEquals(expectedEmail, body.getUser().getEmail());
        assertEquals(expectedName, body.getUser().getName());
    }
}
