package com.tuition;

import com.tuition.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Component
public class DumpAvatarsRunner implements CommandLineRunner {

    private final UserRepository userRepository;

    public DumpAvatarsRunner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        String data = userRepository.findAll().stream()
                .map(u -> u.getUsername() + ": " + u.getAvatarUrl())
                .collect(Collectors.joining("\n"));
        Files.writeString(Paths.get("avatars.txt"), data);
        System.out.println("DUMPED AVATARS TO avatars.txt");
    }
}
