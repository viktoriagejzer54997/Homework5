//Задача 1: написать 4 метода с автотестами, которые будут делать запрос к эндпоинту GET /goods/list учебного API:
//
//1. Метод с использованием инструкций given(), when(), then() — проверить:
//код ответа;
//что тело ответа пустое;

//2. Метод с использованием RequestSpecification — проверить:
//код ответа;
//что тело ответа пустое;

//3. Метод, который будет создавать один товар через эндпоинт POST /goods/add и проверять, что GET /goods/list вернул его в
// списке через встроенные проверки REST Assured.
//
//4. Метод, который будет создавать один товар через эндпоинт POST /goods/add и проверять, что GET /goods/list вернул его в
// списке через AssertJ.

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasItem;

public class Task1 {

    RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost:8080/")
            .setAuth(RestAssured.basic("admin", "secret123"))
            .log(LogDetail.ALL)
            .build();

    @Test
    void test1() {
        given()
                .auth()
                .basic("admin", "secret123")
                .baseUri("http://localhost:8080/")
                .log().all()
                .when()
                .get("/goods/1")
                .then()
                .statusCode(200)
                .body(emptyString())
                .log().body();

    }

    @Test
    void test2() {
        given()
                .spec(requestSpec)
                .when()
                .get("/goods/1")
                .then()
                .statusCode(200)
                .body(emptyString())
                .log().body();
    }

    //3. Метод, который будет создавать один товар через эндпоинт POST /goods/add и проверять, что GET /goods/list вернул его в
// списке через встроенные проверки REST Assured.

@Test
    void test3() {
        String product = "Гречка";
        Integer productPrice = 120;
        String jsonBody = String.format("{\"name\":\"%s\", \"price\":%d}", product, productPrice);
        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post("/goods/add")
                .then()
                .log().body();

        given()
                .spec(requestSpec)
                .when()
                .get("/goods/list")
                .then()
                .body("goods.name", hasItem(product));
}

//4. Метод, который будет создавать один товар через эндпоинт POST /goods/add и проверять, что GET /goods/list вернул его в
// списке через AssertJ.

    @Test
    void test4() {
        String product = "Кошка";
        Integer productPrice = 12000;
        String jsonBody = String.format("{\"name\":\"%s\", \"price\":%d}", product, productPrice);
        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post("/goods/add")
                .then()
                .log().body();

        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/goods/list");
                Assertions.assertThat(response.statusCode())
                        .isEqualTo(200);

        List<String> listProduct = response.jsonPath().getList("goods.name");
        Assertions.assertThat(listProduct)
                        .contains(product);
    }
    }

