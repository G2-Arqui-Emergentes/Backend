package pe.edu.upc.taskmaster.backend.iam.infrastructure.authorization.sfs.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.authorization.sfs.handlers.GoogleOAuth2SuccessHandler;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.tokens.jwt.BearerTokenService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Web Security Configuration.
 * <p>
 * This class is responsible for configuring the web security.
 * It enables the method security and configures the security filter chain.
 * It includes the authentication manager, the authentication provider,
 * the password encoder and the authentication entry point.
 * </p>
 */
@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration {

  private final UserDetailsService userDetailsService;
  private final BearerTokenService tokenService;
  private final BCryptHashingService hashingService;
  private final AuthenticationEntryPoint unauthorizedRequestHandler;
  private final ClientRegistrationRepository clientRegistrationRepository;
  private final GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;

  /**
     * This is the constructor of the class.
     * @param userDetailsService The user details service
     * @param tokenService The token service
     * @param hashingService The hashing service
     * @param authenticationEntryPoint The authentication entry point
     */
  public WebSecurityConfiguration(
          @Qualifier("defaultUserDetailsService") UserDetailsService userDetailsService,
          BearerTokenService tokenService,
          BCryptHashingService hashingService,
          AuthenticationEntryPoint authenticationEntryPoint,
          ClientRegistrationRepository clientRegistrationRepository,
          GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler) {
      this.userDetailsService = userDetailsService;
      this.tokenService = tokenService;
      this.hashingService = hashingService;
      this.unauthorizedRequestHandler = authenticationEntryPoint;
      this.clientRegistrationRepository = clientRegistrationRepository;
      this.googleOAuth2SuccessHandler = googleOAuth2SuccessHandler;
  }

  /**
   * This method creates the Bearer Authorization Request Filter.
   * @return The Bearer Authorization Request Filter
   */
  @Bean
  public BearerAuthorizationRequestFilter authorizationRequestFilter() {
    return new BearerAuthorizationRequestFilter(tokenService, userDetailsService);
  }

  /**
   * This method creates the authentication manager.
   * @param authenticationConfiguration The authentication configuration
   * @return The authentication manager
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  /**
   * This method creates the authentication provider.
   * @return The authentication provider
   */
  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    var authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService);
    authenticationProvider.setPasswordEncoder(hashingService);
    return authenticationProvider;
  }

  /**
   * This method creates the password encoder.
   * @return The password encoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return hashingService;
  }

  @Bean
  public OAuth2AuthorizationRequestResolver oauth2AuthorizationRequestResolver() {
    var resolver = new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            "/oauth2/authorization"
    );
    resolver.setAuthorizationRequestCustomizer(builder -> {
      Map<String, Object> additionalParameters = new HashMap<>();
      additionalParameters.put("access_type", "offline");
      additionalParameters.put("prompt", "consent");
      additionalParameters.put("include_granted_scopes", "true");
      builder.additionalParameters(additionalParameters);
    });
    return resolver;
  }

  /**
   * This method creates the security filter chain.
   * It also configures the http security.
   *
   * @param http The http security
   * @return The security filter chain
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(corsConfigurer -> corsConfigurer.configurationSource( request -> {
      var cors = new CorsConfiguration();
      cors.setAllowedOrigins(List.of(
              "http://localhost:5173",
              "https://taskmaster-web-application.vercel.app"
      ));
      cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
      cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
      cors.setAllowCredentials(true);
      return cors;
    } ));
    http.csrf(csrfConfigurer -> csrfConfigurer.disable())
        .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedRequestHandler))
        .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
                        .authorizationRequestResolver(oauth2AuthorizationRequestResolver()))
                .successHandler(googleOAuth2SuccessHandler))
        .authorizeHttpRequests(
            authorizeRequests -> authorizeRequests
                    .requestMatchers(
                            "/api/v1/google/connect",
                            "/api/v1/authentication/**",
                            "/oauth2/**",
                            "/login/oauth2/**",
                            "/v3/api-docs/**",
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/swagger-resources/**",
                            "/webjars/**",
                            "/actuator/**",
                            "/sw.js",
                            "/error")
                .permitAll()
                .anyRequest()
                .authenticated());
    http.authenticationProvider(authenticationProvider());
    http.addFilterBefore(authorizationRequestFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
