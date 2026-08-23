package com.planwith.planwith_fo_comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.planwith.planwith_fo_comment.config.AuthProperties;
import com.planwith.planwith_fo_comment.config.DeployProperties;
import com.planwith.planwith_fo_comment.config.KafkaTopicProperties;

@SpringBootApplication
@EnableConfigurationProperties({AuthProperties.class, DeployProperties.class, KafkaTopicProperties.class})
public class PlanwithFoCommentApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoCommentApplication.class, args);
	}

}
