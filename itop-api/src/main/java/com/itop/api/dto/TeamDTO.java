package com.itop.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDTO {
    private Long id;
    private String name;
    private Long organizationId;
    private String organizationName;
    private String teamCode;
    private String teamType;
    private Long leaderId;
    private String leaderName;
    private List<Long> memberIds;
    private List<String> memberNames;
    private String email;
    private String phone;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
