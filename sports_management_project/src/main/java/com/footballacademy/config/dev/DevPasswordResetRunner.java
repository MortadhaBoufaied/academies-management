package com.footballacademy.config.dev;

import com.footballacademy.model.User;
import com.footballacademy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**  * Local/dev helper: reset all user passwords to a known value for manual testing.  *  * Enable once via:  * -Dapp.dev.reset-passwords=true  *  * Then restart WITHOUT this property.  */
@Component
@ConditionalOnProperty(name = "app.dev.reset-passwords", havingValue = "true")
public
class DevPasswordResetRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DevPasswordResetRunner.
    class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public DevPasswordResetRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    @Transactional
    public void run(String...args) {
        String raw = "admin123";
        String encoded = passwordEncoder.encode(raw);
        long updated = 0;
        for (User user : userRepository.findAll()) {
            user.setMdp(encoded);
            updated++;
        } userRepository.flush();
        logger.warn("DEV PASSWORD RESET: updated {} users to password '{}'. Disable app.dev.reset-passwords after this run.", updated, raw);
    }
}
