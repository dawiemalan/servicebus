package io.dmcs.servicebus.controller;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "servicebus-controller",
                version = "1.0.0"
        )
)
public class Application {

//    @ContextConfigurer
//    public static class Configurer implements ApplicationContextConfigurer {
//        @Override
//        public void configure(@NonNull ApplicationContextBuilder builder) {
//            builder.defaultEnvironments("dev");
//        }
//    }

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }

//    @Context
//    public ControlPanelModuleConfiguration controlPanelModuleConfiguration() {
//        return new ControlPanelModuleConfiguration() {
//            @Override
//            public boolean isEnabled() {
//                return true;
//            }
//
//            @Override
//            public Set<String> getAllowedEnvironments() {
//                return Set.of("dev", "test", "prod");
//            }
//
//            @Override
//            public String getPath() {
//                return "/control-panel";
//            }
//        };
//    }
}
