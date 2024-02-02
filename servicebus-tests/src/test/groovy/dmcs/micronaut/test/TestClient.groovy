package dmcs.micronaut.test

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client

@Client('http://localhost:20000/api/')
//@FeignClient(url = 'test')
interface TestClient {

    @Get('greet')
    String greeting()
}
