package io.nebulaflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NebulaFlowApplication {
  public static void main(String[] args) { SpringApplication.run(NebulaFlowApplication.class, args); }
}
