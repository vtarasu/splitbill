package com.example.splitbill.user.controller;

import com.example.splitbill.user.dto.*;
import com.example.splitbill.user.service.SettlementsService;
import com.example.splitbill.user.service.UserBalancesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserBalancesController {
    private final UserBalancesService userBalancesService;
    private final SettlementsService settlementsService;

    public UserBalancesController(UserBalancesService userBalancesService, SettlementsService settlementsService) {
        this.userBalancesService = userBalancesService;
        this.settlementsService = settlementsService;
    }

    @GetMapping("/groups")
    public List<GetGroupAndBalances> getUserGroups() {
        var auth = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .orElseThrow(() -> new RuntimeException("Invalid token. user details not found"));
        long userId = (long) auth.getPrincipal();
        log.info("Received request to fetch groups for user={}", userId);
        return userBalancesService.getUserGroupsAndBalances(userId);
    }

    @GetMapping("/balances")
    public List<TotalBalancesDto> getAllBalances() {
        var auth = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .orElseThrow(() -> new RuntimeException("Invalid token. user details not found"));
        long userId = (long) auth.getPrincipal();
        log.info("Received request to fetch all balances for user={}", userId);
        return userBalancesService.getAllOpenBalancesForUser(userId);
    }

    @GetMapping("/nongroup/balances")
    public List<TotalBalancesDto> getNonGroupBalances() {
        var auth = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .orElseThrow(() -> new RuntimeException("Invalid token. user details not found"));
        long userId = (long) auth.getPrincipal();
        log.info("Received request to fetch all balances for user={}", userId);
        return userBalancesService.getNonGroupBalances(userId);
    }

    @PostMapping("/settle")
    public ResponseEntity<?> settleBalance(@RequestBody SettleBalanceRequestDto requestDto) {
        log.info("Received request to settle balances. request={}", requestDto);
        try {
            return ResponseEntity.ok(settlementsService.recordPaymentForUser(requestDto));
        } catch (Exception e) {
            log.error("Error occurred while settling balance. request={}", requestDto, e);
            var errorResponse = ErrorResponse.builder()
                    .status(500)
                    .errorMessage("Error occurred")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @PostMapping("/settle/group")
    public ResponseEntity<?> settleGroupBalance(@RequestBody SettleGroupBalanceRequestDto requestDto) {
        try {
            log.info("Received request to settle balance in group. request={}", requestDto);
            settlementsService.recordPaymentForGroup(requestDto);
            return ResponseEntity.ok("Balance settled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to settle balance in group. error="+e.getLocalizedMessage());
        }

    }

    @GetMapping("/settlements")
    public SettlementHistoryResponseDto getUserSettlements(@RequestParam("pageno") Integer pageNumber,
                                                           @RequestParam("size") Integer pageSize) {
        var userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Received request to fetch settlement history for user={}",userId);
        return settlementsService.getSettlements((Long) userId, pageNumber, pageSize);
    }
}
