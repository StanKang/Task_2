package praktikum.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import praktikum.models.User;
import praktikum.steps.UserSteps;
import praktikum.utils.UserGenerator;

@DisplayName("Логин пользователя")
class LoginUserTest {

    private static final String INCORRECT_CREDENTIALS_MESSAGE = "email or password are incorrect";

    private final UserSteps userSteps = new UserSteps();
    private User user;
    private String token;

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

    @Test
    @DisplayName("Логин под существующим пользователем возвращает успех")
    void loginExistingUserReturnsSuccess() {
        Response response = userSteps.loginUser(user);

        userSteps.checkStatusCode(response, 200);
        userSteps.checkSuccess(response, true);
        userSteps.checkUser(response, user.getEmail(), user.getName());
        userSteps.checkTokensReturned(response);
    }

    @ParameterizedTest(name = "Логин с {0} возвращает ошибку")
    @CsvSource({
            "неверным email, true, false",
            "неверным паролем, false, true",
            "неверными email и паролем, true, true"
    })
    @DisplayName("Логин с неверными данными возвращает ошибку")
    void loginWithIncorrectCredentialsReturnsUnauthorized(String description, boolean wrongEmail, boolean wrongPassword) {
        User credentials = new User(
                wrongEmail ? UserGenerator.generateEmail() : user.getEmail(),
                wrongPassword ? UserGenerator.generatePassword() : user.getPassword(),
                null);

        Response response = userSteps.loginUser(credentials);

        userSteps.checkStatusCode(response, 401);
        userSteps.checkSuccess(response, false);
        userSteps.checkMessage(response, INCORRECT_CREDENTIALS_MESSAGE);
    }
}
