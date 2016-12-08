package pl.epoint.feign;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import rx.Observable;

import java.util.Arrays;
import java.util.List;

/**
 * @author Filip Halemba
 */
@Headers("content-type: application/json")
public interface DeveloperApi {

    @RequestLine("GET /")
    String getAsString();

    @RequestLine("GET /")
    List<Developer> getAsObject();

    @RequestLine("GET /{id}")
    Developer getById(@Param("id") Integer id);

    default Developer getTheBestDeveloper() {
     return getById(2);
    }

    @RequestLine("POST /")
    Developer post(Developer developer);

    @RequestLine("GET /")
    Observable<List<Developer>> beReactive();
}
