package com.example.splitbill.expense.domain;

import com.example.splitbill.group.domain.Group;
import com.example.splitbill.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Data
public class GroupBalances {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupId")
    private Group groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId1")
    private User userId1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId2")
    private User userId2;

    private BigDecimal balance;
}
