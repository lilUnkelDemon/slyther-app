package ir.momeni.slyther.testapi;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public Map<String, String> pub() { return Map.of("msg","عمومی (بدون توکن)"); }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    public Map<String, String> user() { return Map.of("msg","ROLE_USER OK"); }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public Map<String, String> admin() { return Map.of("msg","ROLE_ADMIN OK"); }

    @PreAuthorize("hasRole('SUDO')")
    @GetMapping("/sudo")
    public Map<String, String> sudo() { return Map.of("msg","ROLE_SUDO OK"); }
}
