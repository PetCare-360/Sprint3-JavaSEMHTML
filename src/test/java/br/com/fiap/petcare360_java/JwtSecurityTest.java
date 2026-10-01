package br.com.fiap.petcare360_java;

import java.util.Optional;
import java.util.Map;
import java.time.Instant;
import java.util.Date;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.bind.annotation.*;
import br.com.fiap.petcare360_java.controller.AuthController;
import br.com.fiap.petcare360_java.exception.GlobalExceptionHandler;
import br.com.fiap.petcare360_java.model.*;
import br.com.fiap.petcare360_java.repository.*;
import br.com.fiap.petcare360_java.security.*;
import br.com.fiap.petcare360_java.service.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JwtSecurityTest {
    private static final String SECRET = "test-only-key-012345678901234567890123456789";
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    private AppUserRepository users;
    private AppUser user;

    @BeforeEach
    void setup() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(Config.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean(FilterChainProxy.class)).build();
        users = context.getBean(AppUserRepository.class);
        user = new AppUser();
        user.setId(1L);
        user.setName("Tutor");
        user.setEmail("tutor@example.com");
        user.setEnabled(true);
        user.setPasswordHash(context.getBean(PasswordEncoder.class).encode("senha1234"));
        setRole("ROLE_CLIENTE");
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @AfterEach
    void close() {
        context.close();
    }

    private void setRole(String name) {
        var role = new Role();
        role.setName(name);
        user.getRoles().clear();
        user.getRoles().add(role);
    }

    private String login() throws Exception {
        var result = mvc.perform(post("/auth/login").contentType("application/json")
                .content("{\"email\":\"tutor@example.com\",\"password\":\"senha1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(7200))
                .andExpect(jsonPath("$.user.role").value(user.getRoles().iterator().next().getName()))
                .andExpect(header().doesNotExist("Set-Cookie")).andReturn();
        assertNull(result.getRequest().getSession(false));
        return (String) com.nimbusds.jose.util.JSONObjectUtils
                .parse(result.getResponse().getContentAsString()).get("token");
    }

    @Test
    void loginAndMeWorkWithoutSession() throws Exception {
        String token = login();
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(user.getEmail()));
        mvc.perform(get("/auth/me").cookie(new jakarta.servlet.http.Cookie("JSESSIONID", "old-session")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void badCredentialsAndMalformedTokenReturn401() throws Exception {
        mvc.perform(post("/auth/login").contentType("application/json")
                .content("{\"email\":\"tutor@example.com\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/auth/me").header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profilesAndDisabledAccountsAreCheckedOnEachRequest() throws Exception {
        String token = login();
        mvc.perform(get("/api/iot/probe").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        setRole("ROLE_VETERINARIO");
        token = login();
        mvc.perform(get("/api/iot/probe").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/admin/probe").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        setRole("ROLE_ADMIN");
        token = login();
        mvc.perform(get("/admin/probe").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        user.setEnabled(false);
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredWrongIssuerAndWrongSignatureAreRejected() throws Exception {
        for (String token : new String[] {
                signed(SECRET, "petcare360-api", Instant.now().minusSeconds(120)),
                signed(SECRET, "other-api", Instant.now().plusSeconds(120)),
                signed(SECRET + "-other-key", "petcare360-api", Instant.now().plusSeconds(120))}) {
            mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void registrationIsPublicAndLogoutDocumentsClientRemoval() throws Exception {
        var role = new Role();
        role.setName("ROLE_CLIENTE");
        when(context.getBean(RoleRepository.class).findByName("ROLE_CLIENTE")).thenReturn(Optional.of(role));
        mvc.perform(post("/auth/register").contentType("application/json")
                .content("{\"name\":\"Novo Tutor\",\"email\":\"novo@example.com\",\"password\":\"senha1234\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.user.role").value("ROLE_CLIENTE"));
        String token = login();
        mvc.perform(post("/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(header().doesNotExist("Set-Cookie"));
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void insecureConfigurationFailsFast() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("short", 120));
        assertThrows(IllegalArgumentException.class, () -> new JwtService(SECRET, 0));
    }

    private String signed(String key, String issuer, Instant expiration) throws Exception {
        var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), new JWTClaimsSet.Builder()
                .subject(user.getEmail()).issuer(issuer).expirationTime(Date.from(expiration)).build());
        jwt.sign(new MACSigner(key));
        return jwt.serialize();
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, AuthController.class, AuthService.class, PetMapper.class,
            GlobalExceptionHandler.class, Probe.class})
    static class Config {
        @Bean AppUserRepository users() { return mock(AppUserRepository.class); }
        @Bean RoleRepository roles() { return mock(RoleRepository.class); }
        @Bean JwtService jwtService() { return new JwtService(SECRET, 120); }
    }

    @RestController
    static class Probe {
        @GetMapping({"/api/iot/probe", "/admin/probe"})
        Map<String, Boolean> probe() { return Map.of("ok", true); }
    }
}
