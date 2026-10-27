package praktikum.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import praktikum.models.User;
import praktikum.steps.UserSteps;
import praktikum.utils.UserGenerator;

import java.util.function.Consumer;

@DisplayName("Создание пользователя")
class CreateUserTest {

    private static final String USER_EXISTS_MESSAGE = "User already exists";
    private static final String REQUIRED_FIELDS_MESSAGE = "Email, password and name are required fields";

    private final UserSteps userSteps = new UserSteps();
    private User user;
    private String token;

    enum RequiredField {
        EMAIL("без email", newUser -> newUser.setEmail(null)),
        PASSWORD("без password", newUser -> newUser.setPassword(null)),
        NAME("без name", newUser -> newUser.setName(null));

        private final String description;
        private final Consumer<User> remover;

        RequiredField(String description, Consumer<User> remover) {
            this.description = description;
            this.remover = remover;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    @BeforeEach
    void setUp() {
        user = UserGenerator.generateUser();
    }

    @AfterEach
    void tearDown() {
        if (token != null) {
            userSteps.deleteUser(token);
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя возвращает успех")
    void createUniqueUserReturnsSuccess() {
        Response response = userSteps.createUser(user);
        token = userSteps.getAccessToken(response);

        userSteps.checkStatusCode(response, 200);
        userSteps.checkSuccess(response, true);
        userSteps.checkUser(response, user.getEmail(), user.getName());
        userSteps.checkTokensReturned(response);
    }

    @Test
    @DisplayName("Создание уже зарегистрированного пользователя возвращает ошибку")
    void createExistingUserReturnsForbidden() {
        token = userSteps.getAccessToken(userSteps.createUser(user));

        Response response = userSteps.createUser(user);

        userSteps.checkStatusCode(response, 403);
        userSteps.checkSuccess(response, false);
        userSteps.checkMessage(response, USER_EXISTS_MESSAGE);
    }

    @ParameterizedTest(name = "Создание пользователя {0} возвращает ошибку")
    @EnumSource(RequiredField.class)
    @DisplayName("Создание пользователя без обязательного поля возвращает ошибку")
    void createUserWithoutRequiredFieldReturnsForbidden(RequiredField field) {
        field.remover.accept(user);

        Response response = userSteps.createUser(user);

        userSteps.checkStatusCode(response, 403);
        userSteps.checkSuccess(response, false);
        userSteps.checkMessage(response, REQUIRED_FIELDS_MESSAGE);
    }
}
