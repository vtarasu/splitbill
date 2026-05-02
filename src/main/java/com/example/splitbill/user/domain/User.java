package com.example.splitbill.user.domain;

import com.example.splitbill.group.domain.UserGroup;
import com.example.splitbill.user.dto.CreateUserRequestDto;
import com.example.splitbill.user.dto.UpdateUserDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserGroup> userGroups;

    @CreationTimestamp
    private LocalDateTime dateJoinedAt;

    public static User from(CreateUserRequestDto createUserRequestDto) {
        return User.builder()
                .emailId(createUserRequestDto.getEmailId())
                .username(createUserRequestDto.getUsername())
                .mobileNumber(createUserRequestDto.getMobileNumber())
                .userGroups(new ArrayList<>())
                .build();
    }

    public static User from(UpdateUserDto updateUserDto) {
        return User.builder()
                .id(updateUserDto.getId())
                .emailId(updateUserDto.getEmailId())
                .username(updateUserDto.getUsername())
                .mobileNumber(updateUserDto.getMobileNumber())
                .build();
    }
}
