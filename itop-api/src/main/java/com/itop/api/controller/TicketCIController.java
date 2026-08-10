package com.itop.api.controller;

import com.itop.api.dto.CISummaryDTO;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.ConfigurationItem;
import com.itop.core.entity.TicketCI;
import com.itop.core.repository.ConfigurationItemRepository;
import com.itop.core.repository.TicketCIRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Ticket-CI Association", description = "Ticket and CI association management APIs")
@RestController
@RequestMapping("/ticket-cis")
@RequiredArgsConstructor
public class TicketCIController {

    private final TicketCIRepository ticketCIRepository;
    private final ConfigurationItemRepository ciRepository;

    @Operation(summary = "Add CI association", description = "Associate a configuration item with a ticket")
    @PostMapping
    @PreAuthorize("@securityUtils.hasPermission('ticket:write')")
    public ResponseEntity<ApiResponse<TicketCI>> addAssociation(
            @RequestParam("ticketId") Long ticketId,
            @RequestParam("ciId") Long ciId,
            @RequestParam(value = "relationType", defaultValue = "affects") String relationType) {

        if (ticketCIRepository.findByTicketIdAndCiId(ticketId, ciId) != null) {
            return ResponseEntity.ok(ApiResponse.error(400, "Association already exists"));
        }

        TicketCI ticketCI = new TicketCI(ticketId, ciId, relationType);
        ticketCI = ticketCIRepository.save(ticketCI);
        return ResponseEntity.ok(ApiResponse.success("CI associated successfully", ticketCI));
    }

    @Operation(summary = "Get CIs by ticket", description = "Get all configuration items associated with a ticket")
    @GetMapping("/by-ticket/{ticketId}")
    @PreAuthorize("@securityUtils.hasPermission('ticket:read')")
    public ResponseEntity<ApiResponse<List<CISummaryDTO>>> getCIsByTicket(@PathVariable("ticketId") Long ticketId) {
        List<TicketCI> associations = ticketCIRepository.findByTicketId(ticketId);

        List<CISummaryDTO> cis = associations.stream()
                .map(this::toCISummaryDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(cis));
    }

    @Operation(summary = "Get tickets by CI", description = "Get all tickets associated with a configuration item")
    @GetMapping("/by-ci/{ciId}")
    @PreAuthorize("@securityUtils.hasPermission('ticket:read')")
    public ResponseEntity<ApiResponse<List<Long>>> getTicketsByCI(@PathVariable("ciId") Long ciId) {
        List<TicketCI> associations = ticketCIRepository.findByCiId(ciId);

        List<Long> ticketIds = associations.stream()
                .map(TicketCI::getTicketId)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(ticketIds));
    }

    @Operation(summary = "Remove CI association", description = "Remove association between a ticket and a CI")
    @DeleteMapping
    @PreAuthorize("@securityUtils.hasPermission('ticket:write')")
    public ResponseEntity<ApiResponse<Void>> removeAssociation(
            @RequestParam("ticketId") Long ticketId,
            @RequestParam("ciId") Long ciId) {

        TicketCI association = ticketCIRepository.findByTicketIdAndCiId(ticketId, ciId);
        if (association == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "Association not found"));
        }

        ticketCIRepository.delete(association);
        return ResponseEntity.ok(ApiResponse.success("Association removed", null));
    }

    @Operation(summary = "Remove all CI associations for ticket", description = "Remove all CI associations for a ticket")
    @DeleteMapping("/by-ticket/{ticketId}")
    @PreAuthorize("@securityUtils.hasPermission('ticket:write')")
    public ResponseEntity<ApiResponse<Void>> removeAllByTicket(@PathVariable("ticketId") Long ticketId) {
        ticketCIRepository.deleteByTicketId(ticketId);
        return ResponseEntity.ok(ApiResponse.success("All associations removed", null));
    }

    private CISummaryDTO toCISummaryDTO(TicketCI ticketCI) {
        ConfigurationItem ci = ticketCI.getConfigurationItem();
        if (ci == null) {
            ci = ciRepository.findById(ticketCI.getCiId()).orElse(null);
        }

        if (ci == null) {
            return CISummaryDTO.builder()
                    .id(ticketCI.getCiId())
                    .relationType(ticketCI.getRelationType())
                    .build();
        }

        return CISummaryDTO.builder()
                .id(ci.getId())
                .name(ci.getName())
                .finalClass(ci.getFinalClass())
                .status(ci.getStatus())
                .orgId(ci.getOrganizationId())
                .relationType(ticketCI.getRelationType())
                .build();
    }
}