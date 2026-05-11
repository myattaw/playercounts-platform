package net.playercounts.apigateway.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import net.playercounts.apigateway.dto.request.CreateTagRequest;
import net.playercounts.apigateway.dto.request.UpdateTagRequest;

import net.playercounts.apigateway.dto.response.TagResponse;

import net.playercounts.apigateway.service.admin.AdminTagService;

import org.springframework.http.HttpStatus;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/tags")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTagController {

    private final AdminTagService adminTagService;

    public AdminTagController(
            AdminTagService adminTagService
    ) {
        this.adminTagService = adminTagService;
    }

    @Operation(summary = "Create a server tag")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse createTag(
            @RequestBody CreateTagRequest request
    ) {

        return adminTagService.createTag(request);
    }

    @Operation(summary = "Get all tags")
    @GetMapping
    public List<TagResponse> getTags() {

        return adminTagService.getTags();
    }

    @Operation(summary = "Get tag by ID")
    @GetMapping("/{id}")
    public TagResponse getTag(
            @PathVariable("id") Long id
    ) {

        return adminTagService.getTag(id);
    }

    @Operation(summary = "Update tag")
    @PutMapping("/{id}")
    public TagResponse updateTag(
            @PathVariable("id") Long id,
            @RequestBody UpdateTagRequest request
    ) {

        return adminTagService.updateTag(
                id,
                request
        );
    }

    @Operation(summary = "Delete tag")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(
            @PathVariable("id") Long id
    ) {

        adminTagService.deleteTag(id);
    }

}