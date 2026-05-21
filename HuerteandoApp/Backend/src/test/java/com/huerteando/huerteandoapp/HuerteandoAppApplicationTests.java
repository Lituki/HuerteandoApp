package com.huerteando.huerteandoapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@SpringBootTest(properties = {
		"supabase.url=http://localhost",
		"supabase.key=test-key",
		"supabase.bucket=test-bucket"
})
@ActiveProfiles("h2")
@Import(HuerteandoAppApplicationTests.TestSecurityConfig.class)
class HuerteandoAppApplicationTests {

	@Test
	void contextLoads() {
	}

	@TestConfiguration
	static class TestSecurityConfig {
		@Bean
		JwtDecoder jwtDecoder() {
			return token -> { throw new UnsupportedOperationException("JWT no usado en tests"); };
		}
	}

}
