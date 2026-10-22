package ru.kodacode.billing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                .antMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .anyRequest().authenticated()
            .and()
            .httpBasic();
    }

    @Bean
    public UserDetailsService users(
        @Value("${billing.api.username}") String username,
        @Value("${billing.api.password}") String password
    ) {
        return new InMemoryUserDetailsManager(
            User.withUsername(username).password("{noop}" + password).roles("BILLING").build()
        );
    }
}
