package ch.admin.bit.jeap.jme.cdct;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsumerApplication {

    static void main(String[] args) {

        Environment env = SpringApplication.run(ConsumerApplication.class, args).getEnvironment();

        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        log.info("""
                    ----------------------------------------------------------
                        Yeah!!! {} is running!

                        SwaggerUI: {}://localhost:{}{}/swagger-ui.html?urls.primaryName=public-api
                        Profile(s): {}
                    ----------------------------------------------------------""",
                env.getProperty("spring.application.name"),
                protocol,
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path"),
                env.getActiveProfiles());
    }

}
