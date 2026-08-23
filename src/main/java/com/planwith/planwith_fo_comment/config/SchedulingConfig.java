package com.planwith.planwith_fo_comment.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.outbox.publisher-enabled", havingValue = "true")
public class SchedulingConfig {
}
