package ir.momeni.slyther.testapi;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;



/**
 * Simple test controller exposing public and role-protected endpoints.
 *
 * <p>Security notes:
 * - The {@link PreAuthorize} annotations require method security to be enabled
 *   (e.g., with @EnableMethodSecurity or @EnableGlobalMethodSecurity depending on Spring Security version).
 * - The SpEL expressions use "hasRole('X')" which expects authorities formatted as "ROLE_X".
 *   For example, hasRole('ADMIN') checks for the authority "ROLE_ADMIN".
 */
@RestController
@RequestMapping("/api/test")
public class TestController {


    /**
     * Public endpoint — no authentication token required.
     *
     * @return a simple JSON message confirming public access
     * Example response: {"msg":"Public (no token)"}
     */
    @GetMapping("/public")
    public Map<String, String> pub() { return Map.of("msg", "Public (no token)");}


    /**
     * Endpoint accessible to authenticated users with role USER (i.e., authority "ROLE_USER").
     *
     * @return a confirmation JSON
     * Example response: {"msg":"ROLE_USER OK"}
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    public Map<String, String> user() { return Map.of("msg","ROLE_USER OK"); }



    /**
     * Endpoint restricted to users with role ADMIN (i.e., authority "ROLE_ADMIN").
     *
     * @return a confirmation JSON
     * Example response: {"msg":"ROLE_ADMIN OK"}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public Map<String, String> admin() { return Map.of("msg","ROLE_ADMIN OK"); }


    /**
     * Endpoint restricted to users with role SUDO (i.e., authority "ROLE_SUDO").
     *
     * @return a confirmation JSON
     * Example response: {"msg":"ROLE_SUDO OK"}
     */
    @PreAuthorize("hasRole('SUDO')")
    @GetMapping("/sudo")
    public Map<String, String> sudo() { return Map.of("msg","ROLE_SUDO OK"); }
}
