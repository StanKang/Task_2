package praktikum.steps;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BaseSteps {

    private static final String BASE_URI = System.getProperty(
            "baseUri", "https://qa-stellarburgers.education-services.ru");

    protected RequestSpecification getSpec() {
        return given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)
                .filter(new AllureRestAssured());
    }

    @Step("Проверить, что код ответа равен {expectedCode}")
    public void checkStatusCode(Response response, int expectedCode) {
        assertEquals(expectedCode, response.statusCode());
    }

    @Step("Проверить, что поле success равно {expected}")
    public void checkSuccess(Response response, boolean expected) {
        assertEquals(expected, response.jsonPath().getBoolean("success"));
    }

    @Step("Проверить, что сообщение об ошибке: {expectedMessage}")
    public void checkMessage(Response response, String expectedMessage) {
        assertEquals(expectedMessage, response.jsonPath().getString("message"));
    }

    @Step("Проверить, что тело ответа содержит текст: {expectedText}")
    public void checkBodyContains(Response response, String expectedText) {
        assertTrue(response.asString().contains(expectedText));
    }
}
