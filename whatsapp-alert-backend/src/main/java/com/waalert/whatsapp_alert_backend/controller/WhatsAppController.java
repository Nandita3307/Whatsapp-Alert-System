// package com.waalert.whatsapp_alert_backend.controller;

// import com.waalert.whatsapp_alert_backend.dto.ApiResponse;
// import com.waalert.whatsapp_alert_backend.dto.WhatsAppRequest;
// import com.waalert.whatsapp_alert_backend.service.WhatsAppService;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/whatsapp")
// @RequiredArgsConstructor
// public class WhatsAppController {

//     private final WhatsAppService whatsAppService;

//     @PostMapping("/send")
//     public ResponseEntity<ApiResponse<List<Map<String, Object>>>> send(
//             @Valid @RequestBody WhatsAppRequest request) {
//         return ResponseEntity.ok(ApiResponse.success(
//                 "Messages processed", whatsAppService.sendMessages(request)));
//     }

//     @PostMapping("/generate-message")
//     public ResponseEntity<ApiResponse<String>> generateMessage(@RequestBody Map<String, Object> body) {
//         @SuppressWarnings("unchecked") List<String> columns = (List<String>) body.get("columns");
//         @SuppressWarnings("unchecked") List<List<Object>> rows = (List<List<Object>>) body.get("rows");
//         String header = body.getOrDefault("header", "Query Report").toString();
//         if (columns == null || rows == null)
//             return ResponseEntity.badRequest().body(ApiResponse.error("'columns' and 'rows' required"));
//         return ResponseEntity.ok(ApiResponse.success(
//                 whatsAppService.formatQueryResultAsMessage(columns, rows, header)));
//     }
// }
package com.waalert.whatsapp_alert_backend.controller;

import com.waalert.whatsapp_alert_backend.dto.ApiResponse;
import com.waalert.whatsapp_alert_backend.dto.TemplateSendRequest;
import com.waalert.whatsapp_alert_backend.dto.WhatsAppRequest;
import com.waalert.whatsapp_alert_backend.service.WhatsAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    /** Send a single message to multiple fixed recipients */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> send(
            @Valid @RequestBody WhatsAppRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Messages processed", whatsAppService.sendMessages(request)));
    }

    /**
     * ✅ NEW: Send a personalised template message to each row's phone number.
     *
     * POST /api/whatsapp/send-template
     * Body:
     * {
     *   "template": "Dear {{name}}, your salary for {{month}} is ₹{{amount}}",
     *   "rows": [{ "name": "Kamali", "mob_no": "918610256725", "month": "january", "amount": 26444 }],
     *   "phoneKey": "mob_no"   // optional — auto-detected if omitted
     * }
     */
    @PostMapping("/send-template")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> sendTemplate(
            @RequestBody TemplateSendRequest request) {

        if (request.getTemplate() == null || request.getTemplate().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Template cannot be empty."));
        }
        if (request.getRows() == null || request.getRows().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Rows cannot be empty."));
        }

        List<Map<String, Object>> results = whatsAppService.sendTemplate(request);

        long sent   = results.stream().filter(r -> "SENT".equals(r.get("status"))).count();
        long failed = results.stream().filter(r -> "FAILED".equals(r.get("status"))).count();

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Sent: %d | Failed: %d | Skipped: %d",
                        sent, failed, results.size() - sent - failed),
                results
        ));
    }

    /** Format query results as a single WhatsApp message (existing endpoint) */
    @PostMapping("/generate-message")
    public ResponseEntity<ApiResponse<String>> generateMessage(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked") List<String> columns = (List<String>) body.get("columns");
        @SuppressWarnings("unchecked") List<List<Object>> rows = (List<List<Object>>) body.get("rows");
        String header = body.getOrDefault("header", "Query Report").toString();
        if (columns == null || rows == null)
            return ResponseEntity.badRequest().body(ApiResponse.error("'columns' and 'rows' required"));
        return ResponseEntity.ok(ApiResponse.success(
                whatsAppService.formatQueryResultAsMessage(columns, rows, header)));
    }
}