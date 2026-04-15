package com.example.splitbill.user;

import com.example.splitbill.group.Group;
import com.example.splitbill.group.UserGroup;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "Users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String emailId;
    private String mobileNumber;

    @OneToMany(mappedBy = "user")
    private List<UserGroup> userGroups;

    @CreationTimestamp
    private LocalDateTime dateJoinedAt;

    public static User from(CreateUserRequestDto createUserRequestDto) {
        return User.builder()
                .emailId(createUserRequestDto.getEmailId())
                .username(createUserRequestDto.getUsername())
                .mobileNumber(createUserRequestDto.getMobileNumber())
                .build();
    }
}
