package org.devsahamerlin;

import lombok.extern.slf4j.Slf4j;
import org.devsahamerlin.model.Task;
import org.devsahamerlin.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

@SpringBootApplication (exclude = {SecurityAutoConfiguration.class})
@Slf4j
public class SpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }

    @Bean
    public CommandLineRunner start(TaskRepository taskRepository) {
        return args -> {
            taskRepository.save(Task.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Install and Set Up Jenkins")
                    .description("Install Jenkins on the server and configure it for automated build and deployment tasks.")
                    .build());

            taskRepository.save(Task.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Install and Set Up SonarQube")
                    .description("Install SonarQube and integrate it with the codebase to perform static code analysis and track code quality.")
                    .build());

            taskRepository.save(Task.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Set Up CI/CD Pipeline")
                    .description("Create a CI/CD pipeline using Jenkins that builds, tests, and deploys the application automatically.")
                    .build());

            taskRepository.findAll().forEach(task ->
                    log.info(task.toString()));
        };


    }
}