package com.example.splitbill.expense.domain;

import com.example.splitbill.expense.dto.SplitStrategy;
import com.example.splitbill.group.domain.Group;
import com.example.splitbill.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="added_by")
    private User addedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="paid_by")
    private User paidByUser;

    private BigDecimal billAmount;
    private LocalDate expenseDate;
    private String expense;
    private SplitStrategy splitStrategy;
    private String splitDetails;

    @CreationTimestamp
    private LocalDateTime dateAddedAt;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL)
    private List<ExpenseSplit> split;
}
