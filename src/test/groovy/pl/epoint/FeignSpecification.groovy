package pl.epoint

import feign.Feign
import feign.Logger
import feign.gson.GsonDecoder
import feign.gson.GsonEncoder
import feign.hystrix.HystrixFeign
import pl.epoint.feign.Developer
import pl.epoint.feign.DeveloperApi
import rx.Observable
import rx.observers.TestSubscriber
import spock.lang.Specification


import static java.util.Arrays.asList

/**
 * @author Filip Halemba
 */
class FeignSpecification extends Specification {

    /**
     *  Na początku było API...
     */

    def URL = "http://localhost:8080/dev";
    /**
     *  Zaczynamy
     */

    def "1. Najprostszy przykład Feign"() {
        given:
        def client = Feign.builder().target(DeveloperApi.class, URL)

        when:
        String apiResponse = client.getAsString()

        then:
        apiResponse == """[{"id":1,"name":"Martin Fowler"},{"id":2,"name":"Uncle Bob"}]"""
    }


    def "2. Dodanie loggera"() {
        given:
        def client = Feign.builder()
                          .logger(new Logger.ErrorLogger())
                          .logLevel(Logger.Level.FULL)
                          .target(DeveloperApi.class, URL)

        when:
        String apiResponse = client.getAsString()

        then:
        apiResponse == """[{"id":1,"name":"Martin Fowler"},{"id":2,"name":"Uncle Bob"}]"""
    }


    def String createCorrelationId() {
        return (new Random().nextLong() * 10).toString()
    }

    def "3. Dodanie headera do każdego zapytania"() {
        given:
        def client = Feign.builder()
                          .logger(new Logger.ErrorLogger())
                          .requestInterceptor { it.header("corellation-id", createCorrelationId()) }
                          .logLevel(Logger.Level.FULL)
                          .target(DeveloperApi.class, URL)

        when:
        String apiResponse = client.getAsString()
        client.getAsString()

        then:
        apiResponse == """[{"id":1,"name":"Martin Fowler"},{"id":2,"name":"Uncle Bob"}]"""
    }

    /**
     *  A co z obiektami?
     */

    def UNCLE_BOB = new Developer(2, "Uncle Bob");
    def LIST = asList(new Developer(1, "Martin Fowler"), UNCLE_BOB);
    def clientWithLogger = Feign.builder()
                                .logLevel(Logger.Level.FULL)
                                .logger(new Logger.ErrorLogger())

    def "4. Deserializacja obiektów poprzez decoder"() {
        given:
        def client = clientWithLogger.decoder(new GsonDecoder())
                                     .target(DeveloperApi.class, URL)

        when:
        List<Developer> apiResponse = client.getAsObject()

        then:
        apiResponse == LIST
    }


    def "5. Dodanie parametru do zapytania"() {
        given:
        def client = clientWithLogger.decoder(new GsonDecoder())
                                     .target(DeveloperApi.class, URL)

        when:
        Developer apiResponse = client.getById(2)

        then:
        apiResponse == UNCLE_BOB
    }


    def "6. Domyślne metody w java 8"() {
        given:
        def client = clientWithLogger.decoder(new GsonDecoder())
                                     .target(DeveloperApi.class, URL)

        when:
        Developer apiResponse = client.getTheBestDeveloper()

        then:
        apiResponse == UNCLE_BOB
    }


    def "7. Przykład HTTP post"() {
        given:
        def client = clientWithLogger.decoder(new GsonDecoder())
                                     .encoder(new GsonEncoder())
                                     .target(DeveloperApi.class, URL)

        and:
        def krystian = new Developer(3, "Krystian")

        when:
        Developer apiResponse = client.post(krystian)

        then:
        krystian == apiResponse
    }

    /**
     * Rx examples
     */


    def "8. Bądź reaktywny"() {
        given:
        def client = HystrixFeign.builder().logger(new Logger.ErrorLogger())
                                 .logLevel(Logger.Level.FULL)
                                 .decoder(new GsonDecoder())
                                 .target(DeveloperApi.class, URL)

        when:
        client.beReactive()
              .toBlocking()
              .subscribe { println it }
        then: "Ale nie pisz takich testów"
        true
    }

    def "9. Bądź reaktywny i pisz dobre testy"() {
        given:
        TestSubscriber<Developer> testSubscriber = new TestSubscriber<>();

        and:
        def client = HystrixFeign.builder().logger(new Logger.ErrorLogger())
                                 .logLevel(Logger.Level.FULL)
                                 .decoder(new GsonDecoder())
                                 .target(DeveloperApi.class, URL)

        when:
        client.beReactive()
              .flatMap { Observable.from(it) }
              .subscribe(testSubscriber)

        then:
        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertReceivedOnNext(LIST)
    }

    /*
     *  Dziękuję za uwagę :)
     */
}


