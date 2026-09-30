package com.tuition.repository.spec;

import com.tuition.entity.Role;
import com.tuition.entity.User;
import com.tuition.entity.UserStatus;
import com.tuition.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UserSpecificationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Prepare data
        User u1 = new User();
        u1.setUsername("user1");
        u1.setPassword("pass");
        u1.setRole(Role.STUDENT);
        u1.setStatus(UserStatus.ACTIVE);
        u1.setFullName("Nguyễn Văn An");
        u1.setEmail("an@example.com");
        u1.setPhone("0901234567");

        User u2 = new User();
        u2.setUsername("user2");
        u2.setPassword("pass");
        u2.setRole(Role.STUDENT);
        u2.setStatus(UserStatus.ACTIVE);
        u2.setFullName("Trần Thị Bình");
        u2.setEmail("binh@example.com");
        u2.setPhone("0988776655");

        User u3 = new User();
        u3.setUsername("user3");
        u3.setPassword("pass");
        u3.setRole(Role.STUDENT);
        u3.setStatus(UserStatus.ACTIVE);
        u3.setFullName("Lê Thanh Hải");
        u3.setEmail("thanh_an_123@gmail.com"); // Email contains "an"
        u3.setPhone("0977112233");
        
        User u4 = new User();
        u4.setUsername("user4");
        u4.setPassword("pass");
        u4.setRole(Role.STUDENT);
        u4.setStatus(UserStatus.ACTIVE);
        u4.setFullName("Ngô Quang Phương");
        u4.setEmail("phuong@example.com"); 
        u4.setPhone("090555an55"); // Phone contains "an" (for testing purpose, though not realistic)

        userRepository.saveAll(List.of(u1, u2, u3, u4));
    }

    @Test
    void testSearchKeyword_matchAllConditions() {
        Specification<User> spec = UserSpecification.searchKeyword("an");
        List<User> users = userRepository.findAll(spec);
        
        // "an" matches:
        // u1: fullName ("Nguyễn Văn An")
        // u3: email ("thanh_an_123@gmail.com")
        // u4: phone ("090555an55")
        assertEquals(3, users.size());
    }

    @Test
    void testSearchKeyword_null() {
        Specification<User> spec = UserSpecification.searchKeyword(null);
        List<User> users = userRepository.findAll(spec);
        
        // Null -> return all
        assertEquals(4, users.size());
    }

    @Test
    void testSearchKeyword_empty() {
        Specification<User> spec = UserSpecification.searchKeyword("   ");
        List<User> users = userRepository.findAll(spec);
        
        // Empty string -> return all
        assertEquals(4, users.size());
    }
}
