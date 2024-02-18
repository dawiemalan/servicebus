//file:noinspection GrMethodMayBeStatic
package io.dmcs.micronaut.test


import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.swagger.v3.oas.annotations.Operation
import jakarta.inject.Inject

@Controller('/api/')
@ExecuteOn(TaskExecutors.IO)
//@GenerateClients(id = 'test', destinationModule = 'micronaut-test', destinationFolder = 'src/test/groovy')
class TestController {

    String aProperty
    private String aPrivateProperty

    @Inject
    TestService testService

    protected void protectedMethod() {

    }

    @Operation(summary = 'Some description')
    @Get('/greet')
    String greeting() {
        return 'hello'
    }

    @Get('greet/{id}')
    String greetingWithPathVarableAndQueryParam(@PathVariable('id') String id, @QueryValue('name') String name) {
        return id + '.' + name
    }

    @Post('post')
    String postTest(@Body List<String> theBody) {
        return 'ok' + theBody
    }

    @Put(value = 'put', produces = ['application/json'])
    List<TestPogo> putTest(@Body TestPogo testPogo) {
        return [testPogo]
    }

    @Post('postinner')
    AnInnerClass postInner(@Body AnInnerClass body) {
        return body
    }

    static class AnInnerClass {
        String innerproperty
    }
}
