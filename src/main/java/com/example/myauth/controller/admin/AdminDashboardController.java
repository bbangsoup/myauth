package com.example.myauth.controller.admin;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.admin.dashboard.AdminDashboardSummaryResponse;
import com.example.myauth.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

  private final AdminDashboardService adminDashboardService;

  @GetMapping
  public ResponseEntity<ApiResponse<AdminDashboardSummaryResponse>> getDashboard() {
    AdminDashboardSummaryResponse response = adminDashboardService.getSummary();
    return ResponseEntity.ok(ApiResponse.success("Admin dashboard summary fetched.", response));
  }

  @GetMapping("/summary")
  public ResponseEntity<ApiResponse<AdminDashboardSummaryResponse>> getSummary() {
    AdminDashboardSummaryResponse response = adminDashboardService.getSummary();
    return ResponseEntity.ok(ApiResponse.success("Admin dashboard summary fetched.", response));
  }
}
