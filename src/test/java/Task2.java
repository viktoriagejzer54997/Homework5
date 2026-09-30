import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import static io.restassured.RestAssured.given;

//Задача 2: покрыть автотестами все эндпоинты из тестового API, содержащиеся в Swagger. Требования к автотестам:
//
//автотесты должны проверить все возможные коды ответа (500 не является штатным кодом ответа и проверке не подлежит);
//для каждого автотеста должен быть ассерт;
//должна быть разработана задача для Gradle, которая запускает все API-тесты;
//код должен быть запушен на GitHub в отдельной ветке.
public class Task2 {

    RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost:8080/")
            .setAuth(RestAssured.basic("admin", "secret123"))
            .log(LogDetail.ALL)
            .build();

    //Большой end-to-end тест для проверки статуса 200 (все методы внутри)
    @Test
    void testFullProductLifecycle() {
        Faker fakerRu = new Faker(new Locale("ru"));
        String product = fakerRu.lorem().word();
        Random random = new Random();
        int price = random.nextInt(1, 100);
        String jsonBody = String.format("{\"name\":\"%s\", \"price\":%d}", product, price);
        Response postResponse = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post("/goods/add");
        Assertions.assertThat(postResponse.statusCode())
                .isEqualTo(200);

        int productId = postResponse.jsonPath().getInt("data.id");

        Response getResponse = given()
                .spec(requestSpec)
                .pathParam("id", productId)
                .when()
                .get("/goods/{id}");
        Assertions.assertThat(getResponse.statusCode()).isEqualTo(200);

        String newProduct = fakerRu.lorem().word();
        String jsonBodyNew = String.format("{\"name\":\"%s\", \"price\":%d}", newProduct, price);

        Response patchResponse = given()
                .spec(requestSpec)
                .pathParam("id", productId)
                .contentType(ContentType.JSON)
                .body(jsonBodyNew)
                .when()
                .patch("/goods/{id}");
        Assertions.assertThat(patchResponse.statusCode())
                .isEqualTo(200);

        Response allProduct = given()
                .spec(requestSpec)
                .when()
                .get("/goods/list");
        Assertions.assertThat(allProduct.statusCode()).isEqualTo(200);

        Response deleteResponse = given()
                .spec(requestSpec)
                .pathParam("id", productId)
                .when()
                .delete("/goods/{id}");
        Assertions.assertThat(deleteResponse.statusCode()).isEqualTo(200);
    }

//Проверка на 400 статус метода post /goods/add
    @Test
    void testAddProduct400() {
        Faker fakerRu = new Faker(new Locale("ru"));
        String product = fakerRu.lorem().word();
        Random random = new Random();
        int price = random.nextInt(-100, -1);
        String jsonBody = String.format("{\"name\":\"%s\", \"price\":%d}", product, price);
        Response response = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post("/goods/add");
        Assertions.assertThat(response.statusCode())
                .isEqualTo(400);
    }

    //
    @Test
    void testGetProduct404() {
        Faker fakerRu = new Faker(new Locale("ru"));
        String product = fakerRu.lorem().word();
        Random random = new Random();
        int price = random.nextInt(1, 100);
        String jsonBody = String.format("{\"name\":\"%s\", \"price\":%d}", product, price);

        //добавляем товар
        Response postResponse = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post("/goods/add");

        //удаляем товар
        int productId = postResponse.jsonPath().getInt("data.id");
        given()
                .spec(requestSpec)
                .pathParam("id", productId)
                .when()
                .delete("/goods/{id}");

        //проверяем что товара нет
        Response getResponse = given()
                .spec(requestSpec)
                .pathParam("id", productId)
                .when()
                .get("/goods/{id}");
        Assertions.assertThat(getResponse.statusCode()).isEqualTo(404);
    }

    //проверка 404 ошибки при удалении
    @Test
    void testDeleteProduct404() {
        Faker fakerRu = new Faker(new Locale("ru"));
        String product = fakerRu.lorem().word();
        Random random = new Random();
        int price = random.nextInt(1, 100);
        String jsonBody = String.format("{\"name\":\"%s\", \"price\":%d}", product, price);
        Response postResponse = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post("/goods/add");

        // удаляем товар чтоб проверить что его нет и вернется 404 ошибка
        int productId = postResponse.jsonPath().getInt("data.id");
        given()
                .spec(requestSpec)
                .pathParam("id", productId)
                .when()
                .delete("/goods/{id}");

        Response deleteResponse = given()
                .spec(requestSpec)
                .pathParam("id", productId)
                .when()
                .delete("/goods/{id}");
        Assertions.assertThat(deleteResponse.statusCode()).isEqualTo(404);
    }

    //проверка 400 ошибки при обновлении товара
    @Test
    void patchProduct400() {
        Faker fakerRu = new Faker(new Locale("ru"));
        String product = fakerRu.lorem().word();
        Random random = new Random();
        int price = random.nextInt(1, 100);
        String jsonBody = String.format("{\"name\":\"%s\", \"price\":%d}", product, price);
        Response postResponse = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post("/goods/add");
        //отправляем в теле только price, без name чтоб словить ошибку
        int productId = postResponse.jsonPath().getInt("data.id");
        String jsonBodyNew = String.format("{\"price\":%d}", price);
        Response patchResponse = given()
                .spec(requestSpec)
                .pathParam("id", productId)
                .contentType(ContentType.JSON)
                .body(jsonBodyNew)
                .when()
                .patch("/goods/{id}");
        Assertions.assertThat(patchResponse.statusCode())
                .isEqualTo(400);
    }

//404 ошибка при обновлении товара
    @Test
    void patchProduct404() {
        Faker fakerRu = new Faker(new Locale("ru"));
        String product = fakerRu.lorem().word();
        Random random = new Random();
        int price = random.nextInt(1, 100);
        String jsonBody = String.format("{\"name\":\"%s\", \"price\":%d}", product, price);
        Response postResponse = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post("/goods/add");

        int productId = postResponse.jsonPath().getInt("data.id");

        //удаляем товар чтоб словить 404 ошибку
        given()
                .spec(requestSpec)
                .pathParam("id", productId)
                .when()
                .delete("/goods/{id}");

        Response patchResponse = given()
                .spec(requestSpec)
                .pathParam("id", productId)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .patch("/goods/{id}");
        Assertions.assertThat(patchResponse.statusCode())
                .isEqualTo(404);
    }

    //проверяем ошибку авторизации
    @Test
    void testPostList401() {
        Faker fakerRu = new Faker(new Locale("ru"));
        String product = fakerRu.lorem().word();
        Random random = new Random();
        int price = random.nextInt(1, 100);
        String jsonBody = String.format("{\"name\":\"%s\", \"price\":%d}", product, price);
        Response postResponse = given()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post("/goods/add");
        Assertions.assertThat(postResponse.statusCode())
                .isEqualTo(401);
    }


}
