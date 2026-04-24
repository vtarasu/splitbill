package com.example.splitbill.group.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name="groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String groupName;
    private String groupDescription;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<UserGroup> users;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
