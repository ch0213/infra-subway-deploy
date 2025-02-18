package nextstep.subway.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ApiController {
    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    private Properties properties;

    public ApiController(Properties properties) {
        this.properties = properties;
    }

    @GetMapping("/properties")
    public String index() {
        String name = properties.getName();
        String password = properties.getPassword();
        log.info("user name : {}, password : {}", name, password);
        return "안녕하세요 : " + name;
    }
}
