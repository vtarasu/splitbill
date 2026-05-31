package com.example.splitbill.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Settlements")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Settlements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_id")
    private User from;

    @ManyToOne
    @JoinColumn(name = "to_id")
    private User to;

    private BigDecimal amount;

    @CreationTimestamp
    private LocalDateTime settledAt;
}
