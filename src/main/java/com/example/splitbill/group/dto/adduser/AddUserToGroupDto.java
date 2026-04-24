package com.example.splitbill.group.dto.adduser;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AddUserToGroupDto {
    private List<Long> userId;
    private Long groupId;
}
