package com.planwith.planwith_fo_comment.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

	@Value("${app.gateway.public-url:/}")
	private String gatewayPublicUrl;

	@Bean
	public OpenAPI planwith_fo_commentOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("PlanWith planwith-fo-comment API")
						.description("""
								Call APIs through the API Gateway (:8000).
								Do not put Docker hostname or :8090 in OpenAPI servers.
								Swagger Try-it-out must use the browser origin (Gateway).
								""")
						.version("v1"))
				.servers(List.of(new Server()
						.url(gatewayPublicUrl)
						.description("API Gateway")));
	}
}
