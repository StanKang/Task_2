package praktikum.utils;

import praktikum.models.User;

import java.util.UUID;

public class UserGenerator {

    private UserGenerator() {
    }

    public static User generateUser() {
        return new User(generateEmail(), generatePassword(), generateName());
    }

    public static String generateEmail() {
        return "test-" + UUID.randomUUID() + "@yandex.ru";
    }

    public static String generatePassword() {
        return "Pwd-" + UUID.randomUUID().toString().substring(0, 12);
    }

    public static String generateName() {
        return "Name-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
