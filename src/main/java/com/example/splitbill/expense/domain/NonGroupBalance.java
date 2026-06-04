package com.example.splitbill.expense.domain;

import com.example.splitbill.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
public class NonGroupBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fromId")
    private User from;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toId")
    private User to;

    private BigDecimal balance;
}
