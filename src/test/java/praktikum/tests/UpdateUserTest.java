package praktikum.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import praktikum.models.User;
import praktikum.steps.UserSteps;
import praktikum.utils.UserGenerator;

import java.util.function.Supplier;

@DisplayName("Изменение данных пользователя")
class UpdateUserTest {

    private static final String UNAUTHORISED_MESSAGE = "You should be authorised";

    private final UserSteps userSteps = new UserSteps();
    private User user;
    private String token;

    enum UserField {
        EMAIL("email", () -> new User(UserGenerator.generateEmail(), null, null)),
        NAME("имени", () -> new User(null, null, UserGenerator.generateName())),
        PASSWORD("пароля", () -> new User(null, UserGenerator.generatePassword(), null));

        private final String description;
        private final Supplier<User> updateSupplier;

        UserField(String description, Supplier<User> updateSupplier) {
            this.description = description;
            this.updateSupplier = updateSupplier;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    @BeforeEach
    void setUp() {
        user = UserGenerator.generateUser();
        token = userSteps.getAccessToken(userSteps.createUser(user));
    }

    @AfterEach
    void tearDown() {
        if (token != null) {
            userSteps.deleteUser(token);
        }
    }

    @ParameterizedTest(name = "Изменение {0} с авторизацией возвращает успех")
    @EnumSource(UserField.class)
    @DisplayName("Изменение данных пользователя с авторизацией")
    void updateUserWithAuthReturnsSuccess(UserField field) {
        User update = field.updateSupplier.get();
        String expectedEmail = update.getEmail() != null ? update.getEmail() : user.getEmail();
        String expectedName = update.getName() != null ? update.getName() : user.getName();

        Response response = userSteps.updateUser(token, update);

        userSteps.checkStatusCode(response, 200);
        userSteps.checkSuccess(response, true);
        userSteps.checkUser(response, expectedEmail, expectedName);
        if (field == UserField.PASSWORD) {
            Response loginResponse = userSteps.loginUser(new User(user.getEmail(), update.getPassword(), null));

            userSteps.checkStatusCode(loginResponse, 200);
            userSteps.checkSuccess(loginResponse, true);
        }
    }

    @ParameterizedTest(name = "Изменение {0} без авторизации возвращает ошибку")
    @EnumSource(UserField.class)
    @DisplayName("Изменение данных пользователя без авторизации")
    void updateUserWithoutAuthReturnsUnauthorized(UserField field) {
        User update = field.updateSupplier.get();

        Response response = userSteps.updateUserWithoutAuth(update);

        userSteps.checkStatusCode(response, 401);
        userSteps.checkSuccess(response, false);
        userSteps.checkMessage(response, UNAUTHORISED_MESSAGE);
    }
}
