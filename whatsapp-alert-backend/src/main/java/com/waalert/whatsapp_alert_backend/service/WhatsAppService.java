// // // package com.waalert.whatsapp_alert_backend.service;

// // // import com.waalert.whatsapp_alert_backend.dto.WhatsAppRequest;
// // // import com.waalert.whatsapp_alert_backend.entity.WhatsAppLog;
// // // import com.waalert.whatsapp_alert_backend.repository.WhatsAppLogRepository;
// // // import lombok.extern.slf4j.Slf4j;
// // // import org.springframework.beans.factory.annotation.Qualifier;
// // // import org.springframework.beans.factory.annotation.Value;
// // // import org.springframework.http.HttpHeaders;
// // // import org.springframework.http.MediaType;
// // // import org.springframework.stereotype.Service;
// // // import org.springframework.web.reactive.function.client.WebClient;
// // // import org.springframework.web.reactive.function.client.WebClientResponseException;

// // // import java.util.*;

// // // @Service
// // // @Slf4j
// // // public class WhatsAppService {

// // //     private final WhatsAppLogRepository logRepository;
// // //     private final WebClient webClient;

// // //     @Value("${whatsapp.access-token}")
// // //     private String accessToken;

// // //     @Value("${whatsapp.phone-number-id}")
// // //     private String phoneNumberId;

// // //     public WhatsAppService(WhatsAppLogRepository logRepository,
// // //                            @Qualifier("whatsAppWebClient") WebClient webClient) {
// // //         this.logRepository = logRepository;
// // //         this.webClient     = webClient;
// // //     }

// // //     public List<Map<String, Object>> sendMessages(WhatsAppRequest request) {
// // //         List<Map<String, Object>> results = new ArrayList<>();
// // //         for (String raw : request.getRecipients()) {
// // //             String phone = sanitize(raw);
// // //             WhatsAppLog entry = WhatsAppLog.builder()
// // //                     .recipient(phone)
// // //                     .messageBody(request.getMessage())
// // //                     .status(WhatsAppLog.Status.PENDING)
// // //                     .build();
// // //             try {
// // //                 Map<String, Object> response = callApi(buildPayload(phone, request.getMessage()));
// // //                 String mid = extractId(response);
// // //                 entry.setStatus(WhatsAppLog.Status.SENT);
// // //                 entry.setWaMessageId(mid);
// // //                 results.add(Map.of("recipient", phone, "status", "SENT", "messageId", mid));
// // //                 log.info("WhatsApp sent to {} — id={}", phone, mid);
// // //             } catch (WebClientResponseException e) {
// // //                 String err = "Meta API " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
// // //                 entry.setStatus(WhatsAppLog.Status.FAILED);
// // //                 entry.setErrorMessage(err);
// // //                 results.add(Map.of("recipient", phone, "status", "FAILED", "error", err));
// // //                 log.error("WhatsApp failed for {}: {}", phone, err);
// // //             } catch (Exception e) {
// // //                 entry.setStatus(WhatsAppLog.Status.FAILED);
// // //                 entry.setErrorMessage(e.getMessage());
// // //                 results.add(Map.of("recipient", phone, "status", "FAILED", "error", e.getMessage()));
// // //             } finally {
// // //                 logRepository.save(entry);
// // //             }
// // //         }
// // //         return results;
// // //     }

// // //     public String formatQueryResultAsMessage(List<String> columns, List<List<Object>> rows, String header) {
// // //         StringBuilder sb = new StringBuilder();
// // //         sb.append("📊 *").append(header).append("*\n");
// // //         sb.append("━".repeat(28)).append("\n");
// // //         sb.append("*").append(String.join(" | ", columns)).append("*\n").append("─".repeat(28)).append("\n");
// // //         int limit = Math.min(rows.size(), 20);
// // //         for (int i = 0; i < limit; i++) {
// // //             List<String> vals = new ArrayList<>();
// // //             for (Object v : rows.get(i)) vals.add(v != null ? v.toString() : "-");
// // //             sb.append(String.join(" | ", vals)).append("\n");
// // //         }
// // //         if (rows.size() > 20) sb.append("_...and ").append(rows.size() - 20).append(" more rows_\n");
// // //         sb.append("━".repeat(28)).append("\n_Sent by WA Alert System_");
// // //         return sb.toString();
// // //     }

// // //     private Map<String, Object> buildPayload(String to, String body) {
// // //         return Map.of(
// // //             "messaging_product", "whatsapp",
// // //             "recipient_type",    "individual",
// // //             "to",                to,
// // //             "type",              "text",
// // //             "text",              Map.of("body", body, "preview_url", false)
// // //         );
// // //     }

// // //     @SuppressWarnings("unchecked")
// // //     private Map<String, Object> callApi(Map<String, Object> payload) {
// // //         return webClient.post()
// // //                 .uri("/{phoneNumberId}/messages", phoneNumberId)
// // //                 .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
// // //                 .contentType(MediaType.APPLICATION_JSON)
// // //                 .bodyValue(payload)
// // //                 .retrieve()
// // //                 .bodyToMono(Map.class)
// // //                 .block();
// // //     }

// // //     @SuppressWarnings("unchecked")
// // //     private String extractId(Map<String, Object> response) {
// // //         if (response == null) return "unknown";
// // //         List<Map<String, Object>> messages = (List<Map<String, Object>>) response.get("messages");
// // //         if (messages != null && !messages.isEmpty()) {
// // //             Object id = messages.get(0).get("id");
// // //             return id != null ? id.toString() : "unknown";
// // //         }
// // //         return "unknown";
// // //     }

// // //     private String sanitize(String phone) {
// // //         return phone.replaceAll("[\\s\\-()]+", "").replaceAll("^\\+", "");
// // //     }
// // // }
// // package com.waalert.whatsapp_alert_backend.service;

// // import com.waalert.whatsapp_alert_backend.dto.TemplateSendRequest;
// // import com.waalert.whatsapp_alert_backend.dto.WhatsAppRequest;
// // import com.waalert.whatsapp_alert_backend.entity.WhatsAppLog;
// // import com.waalert.whatsapp_alert_backend.repository.WhatsAppLogRepository;
// // import lombok.extern.slf4j.Slf4j;
// // import org.springframework.beans.factory.annotation.Qualifier;
// // import org.springframework.beans.factory.annotation.Value;
// // import org.springframework.http.HttpHeaders;
// // import org.springframework.http.MediaType;
// // import org.springframework.stereotype.Service;
// // import org.springframework.web.reactive.function.client.WebClient;
// // import org.springframework.web.reactive.function.client.WebClientResponseException;

// // import java.util.*;
// // import java.util.regex.Matcher;
// // import java.util.regex.Pattern;

// // @Service
// // @Slf4j
// // public class WhatsAppService {

// //     private final WhatsAppLogRepository logRepository;
// //     private final WebClient webClient;

// //     @Value("${whatsapp.access-token}")
// //     private String accessToken;

// //     @Value("${whatsapp.phone-number-id}")
// //     private String phoneNumberId;

// //     /** Column names treated as phone number fields (case-insensitive). */
// //     private static final List<String> PHONE_KEYS = List.of(
// //         "mob_no", "mobile", "phone", "whatsapp_number", "mobile_no",
// //         "contact", "phone_no", "cell", "cell_no", "contact_no"
// //     );

// //     public WhatsAppService(WhatsAppLogRepository logRepository,
// //                            @Qualifier("whatsAppWebClient") WebClient webClient) {
// //         this.logRepository = logRepository;
// //         this.webClient     = webClient;
// //     }

// //     // ─────────────────────────────────────────────────────────
// //     // Existing: send list of messages to fixed recipients
// //     // ─────────────────────────────────────────────────────────

// //     public List<Map<String, Object>> sendMessages(WhatsAppRequest request) {
// //         List<Map<String, Object>> results = new ArrayList<>();
// //         for (String raw : request.getRecipients()) {
// //             String phone = sanitize(raw);
// //             WhatsAppLog entry = WhatsAppLog.builder()
// //                     .recipient(phone)
// //                     .messageBody(request.getMessage())
// //                     .status(WhatsAppLog.Status.PENDING)
// //                     .build();
// //             try {
// //                 Map<String, Object> response = callApi(buildPayload(phone, request.getMessage()));
// //                 String mid = extractId(response);
// //                 entry.setStatus(WhatsAppLog.Status.SENT);
// //                 entry.setWaMessageId(mid);
// //                 results.add(Map.of("recipient", phone, "status", "SENT", "messageId", mid));
// //                 log.info("WhatsApp sent to {} — id={}", phone, mid);
// //             } catch (WebClientResponseException e) {
// //                 String err = "Meta API " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
// //                 entry.setStatus(WhatsAppLog.Status.FAILED);
// //                 entry.setErrorMessage(err);
// //                 results.add(Map.of("recipient", phone, "status", "FAILED", "error", err));
// //                 log.error("WhatsApp failed for {}: {}", phone, err);
// //             } catch (Exception e) {
// //                 entry.setStatus(WhatsAppLog.Status.FAILED);
// //                 entry.setErrorMessage(e.getMessage());
// //                 results.add(Map.of("recipient", phone, "status", "FAILED", "error", e.getMessage()));
// //             } finally {
// //                 logRepository.save(entry);
// //             }
// //         }
// //         return results;
// //     }

// //     // ─────────────────────────────────────────────────────────
// //     // NEW: Template-based sending — one personalised message per row
// //     // POST /api/whatsapp/send-template
// //     // ─────────────────────────────────────────────────────────

// //     /**
// //      * For each row in the request:
// //      *  1. Find the phone number column.
// //      *  2. Replace {{placeholders}} in the template with row values.
// //      *  3. Send a personalised WhatsApp message.
// //      *
// //      * @return list of per-recipient results with status
// //      */
// //     public List<Map<String, Object>> sendTemplate(TemplateSendRequest request) {
// //         List<Map<String, Object>> results = new ArrayList<>();

// //         if (request.getRows() == null || request.getRows().isEmpty()) {
// //             return results;
// //         }

// //         for (Map<String, Object> row : request.getRows()) {
// //             // 1 — Detect phone number
// //             String phone = resolvePhone(row, request.getPhoneKey());
// //             if (phone == null || phone.isBlank()) {
// //                 results.add(Map.of("status", "SKIPPED", "reason", "No phone number found in row", "row", row));
// //                 continue;
// //             }
// //             phone = sanitize(phone);

// //             // 2 — Personalise template
// //             String message = fillTemplate(request.getTemplate(), row);

// //             // 3 — Send
// //             WhatsAppLog entry = WhatsAppLog.builder()
// //                     .recipient(phone)
// //                     .messageBody(message)
// //                     .status(WhatsAppLog.Status.PENDING)
// //                     .build();
// //             try {
// //                 Map<String, Object> response = callApi(buildPayload(phone, message));
// //                 String mid = extractId(response);
// //                 entry.setStatus(WhatsAppLog.Status.SENT);
// //                 entry.setWaMessageId(mid);
// //                 results.add(Map.of("recipient", phone, "status", "SENT",
// //                                    "messageId", mid, "message", message));
// //                 log.info("Template sent to {} — id={}", phone, mid);
// //             } catch (WebClientResponseException e) {
// //                 String err = "Meta API " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
// //                 entry.setStatus(WhatsAppLog.Status.FAILED);
// //                 entry.setErrorMessage(err);
// //                 results.add(Map.of("recipient", phone, "status", "FAILED", "error", err));
// //                 log.error("Template send failed for {}: {}", phone, err);
// //             } catch (Exception e) {
// //                 entry.setStatus(WhatsAppLog.Status.FAILED);
// //                 entry.setErrorMessage(e.getMessage());
// //                 results.add(Map.of("recipient", phone, "status", "FAILED", "error", e.getMessage()));
// //             } finally {
// //                 logRepository.save(entry);
// //             }
// //         }
// //         return results;
// //     }

// //     // ─────────────────────────────────────────────────────────
// //     // Template engine — replaces {{key}} with row[key]
// //     // ─────────────────────────────────────────────────────────

// //     private String fillTemplate(String template, Map<String, Object> row) {
// //         if (template == null) return "";
// //         Pattern p = Pattern.compile("\\{\\{(\\w+)}}");
// //         Matcher m = p.matcher(template);
// //         StringBuilder sb = new StringBuilder();
// //         while (m.find()) {
// //             String key = m.group(1);
// //             // Case-insensitive lookup
// //             String value = row.entrySet().stream()
// //                     .filter(e -> e.getKey().equalsIgnoreCase(key))
// //                     .map(e -> e.getValue() != null ? e.getValue().toString() : "")
// //                     .findFirst()
// //                     .orElse("{{" + key + "}}");        // leave placeholder if not found
// //             m.appendReplacement(sb, Matcher.quoteReplacement(value));
// //         }
// //         m.appendTail(sb);
// //         return sb.toString();
// //     }

// //     /**
// //      * Resolve the phone number from a row map.
// //      * Uses the explicit phoneKey if provided, otherwise auto-detects.
// //      */
// //     private String resolvePhone(Map<String, Object> row, String phoneKey) {
// //         if (phoneKey != null && !phoneKey.isBlank()) {
// //             Object v = row.entrySet().stream()
// //                     .filter(e -> e.getKey().equalsIgnoreCase(phoneKey))
// //                     .map(Map.Entry::getValue)
// //                     .findFirst().orElse(null);
// //             return v != null ? v.toString() : null;
// //         }
// //         // Auto-detect from known aliases
// //         for (String alias : PHONE_KEYS) {
// //             for (Map.Entry<String, Object> entry : row.entrySet()) {
// //                 if (entry.getKey().equalsIgnoreCase(alias) && entry.getValue() != null) {
// //                     return entry.getValue().toString();
// //                 }
// //             }
// //         }
// //         return null;
// //     }

// //     // ─────────────────────────────────────────────────────────
// //     // Existing utility methods (unchanged)
// //     // ─────────────────────────────────────────────────────────

// //     public String formatQueryResultAsMessage(List<String> columns, List<List<Object>> rows, String header) {
// //         StringBuilder sb = new StringBuilder();
// //         sb.append("📊 *").append(header).append("*\n");
// //         sb.append("━".repeat(28)).append("\n");
// //         sb.append("*").append(String.join(" | ", columns)).append("*\n").append("─".repeat(28)).append("\n");
// //         int limit = Math.min(rows.size(), 20);
// //         for (int i = 0; i < limit; i++) {
// //             List<String> vals = new ArrayList<>();
// //             for (Object v : rows.get(i)) vals.add(v != null ? v.toString() : "-");
// //             sb.append(String.join(" | ", vals)).append("\n");
// //         }
// //         if (rows.size() > 20) sb.append("_...and ").append(rows.size() - 20).append(" more rows_\n");
// //         sb.append("━".repeat(28)).append("\n_Sent by WA Alert System_");
// //         return sb.toString();
// //     }

// //     private Map<String, Object> buildPayload(String to, String body) {
// //         return Map.of(
// //             "messaging_product", "whatsapp",
// //             "recipient_type",    "individual",
// //             "to",                to,
// //             "type",              "text",
// //             "text",              Map.of("body", body, "preview_url", false)
// //         );
// //     }

// //     @SuppressWarnings("unchecked")
// //     private Map<String, Object> callApi(Map<String, Object> payload) {
// //         return webClient.post()
// //                 .uri("/{phoneNumberId}/messages", phoneNumberId)
// //                 .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
// //                 .contentType(MediaType.APPLICATION_JSON)
// //                 .bodyValue(payload)
// //                 .retrieve()
// //                 .bodyToMono(Map.class)
// //                 .block();
// //     }

// //     @SuppressWarnings("unchecked")
// //     private String extractId(Map<String, Object> response) {
// //         if (response == null) return "unknown";
// //         List<Map<String, Object>> messages = (List<Map<String, Object>>) response.get("messages");
// //         if (messages != null && !messages.isEmpty()) {
// //             Object id = messages.get(0).get("id");
// //             return id != null ? id.toString() : "unknown";
// //         }
// //         return "unknown";
// //     }

// //     private String sanitize(String phone) {
// //         return phone.replaceAll("[\\s\\-()]+", "").replaceAll("^\\+", "");
// //     }
// // }
// package com.waalert.whatsapp_alert_backend.service;

// import com.waalert.whatsapp_alert_backend.dto.TemplateSendRequest;
// import com.waalert.whatsapp_alert_backend.dto.WhatsAppRequest;
// import com.waalert.whatsapp_alert_backend.entity.WhatsAppLog;
// import com.waalert.whatsapp_alert_backend.repository.WhatsAppLogRepository;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.core.io.ByteArrayResource;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.http.client.MultipartBodyBuilder;
// import org.springframework.stereotype.Service;
// import org.springframework.web.reactive.function.client.WebClient;
// import org.springframework.web.reactive.function.client.WebClientResponseException;

// import java.util.*;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;

// /**
//  * WhatsAppService
//  * ──────────────────────────────────────────────────────────────
//  * Handles all WhatsApp message sending via Meta Cloud API.
//  *
//  * Methods:
//  *  sendMessages()         - plain text to fixed recipients
//  *  sendTemplate()         - personalised per-row template messages
//  *  sendPdfAlert()         ← NEW: send PDF as document attachment + caption
//  *  formatQueryResultAsMessage() - format table as WhatsApp text
//  *  uploadMedia()          ← NEW: upload PDF to Meta media endpoint
//  */
// @Service
// @Slf4j
// public class WhatsAppService {

//     private final WhatsAppLogRepository logRepository;
//     private final WebClient             webClient;

//     @Value("${whatsapp.access-token}")
//     private String accessToken;

//     @Value("${whatsapp.phone-number-id}")
//     private String phoneNumberId;

//     /** Columns treated as phone number fields (case-insensitive). */
//     private static final List<String> PHONE_KEYS = List.of(
//         "mob_no", "mobile", "phone", "whatsapp_number", "mobile_no",
//         "contact", "phone_no", "cell", "cell_no", "contact_no"
//     );

//     public WhatsAppService(WhatsAppLogRepository logRepository,
//                            @Qualifier("whatsAppWebClient") WebClient webClient) {
//         this.logRepository = logRepository;
//         this.webClient     = webClient;
//     }

//     // ─────────────────────────────────────────────────────────
//     // sendMessages() — plain text to fixed recipients (existing, unchanged)
//     // ─────────────────────────────────────────────────────────

//     public List<Map<String, Object>> sendMessages(WhatsAppRequest request) {
//         List<Map<String, Object>> results = new ArrayList<>();
//         for (String raw : request.getRecipients()) {
//             String phone = sanitize(raw);
//             WhatsAppLog entry = WhatsAppLog.builder()
//                     .recipient(phone)
//                     .messageBody(request.getMessage())
//                     .status(WhatsAppLog.Status.PENDING)
//                     .build();
//             try {
//                 Map<String, Object> response = callTextApi(phone, request.getMessage());
//                 String mid = extractId(response);
//                 entry.setStatus(WhatsAppLog.Status.SENT);
//                 entry.setWaMessageId(mid);
//                 results.add(Map.of("recipient", phone, "status", "SENT", "messageId", mid));
//                 log.info("WhatsApp text sent to {} — id={}", phone, mid);
//             } catch (WebClientResponseException e) {
//                 String err = "Meta API " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
//                 entry.setStatus(WhatsAppLog.Status.FAILED);
//                 entry.setErrorMessage(err);
//                 results.add(Map.of("recipient", phone, "status", "FAILED", "error", err));
//                 log.error("Text send failed for {}: {}", phone, err);
//             } catch (Exception e) {
//                 entry.setStatus(WhatsAppLog.Status.FAILED);
//                 entry.setErrorMessage(e.getMessage());
//                 results.add(Map.of("recipient", phone, "status", "FAILED", "error", e.getMessage()));
//             } finally {
//                 logRepository.save(entry);
//             }
//         }
//         return results;
//     }

//     // ─────────────────────────────────────────────────────────
//     // sendTemplate() — personalised per-row messages (existing, unchanged)
//     // ─────────────────────────────────────────────────────────

//     public List<Map<String, Object>> sendTemplate(TemplateSendRequest request) {
//         List<Map<String, Object>> results = new ArrayList<>();
//         if (request.getRows() == null || request.getRows().isEmpty()) return results;

//         for (Map<String, Object> row : request.getRows()) {
//             String phone = resolvePhone(row, request.getPhoneKey());
//             if (phone == null || phone.isBlank()) {
//                 results.add(Map.of("status", "SKIPPED",
//                                    "reason", "No phone number found in row", "row", row));
//                 continue;
//             }
//             phone = sanitize(phone);
//             String message = fillTemplate(request.getTemplate(), row);

//             WhatsAppLog entry = WhatsAppLog.builder()
//                     .recipient(phone).messageBody(message)
//                     .status(WhatsAppLog.Status.PENDING).build();
//             try {
//                 Map<String, Object> response = callTextApi(phone, message);
//                 String mid = extractId(response);
//                 entry.setStatus(WhatsAppLog.Status.SENT);
//                 entry.setWaMessageId(mid);
//                 results.add(Map.of("recipient", phone, "status", "SENT",
//                                    "messageId", mid, "message", message));
//                 log.info("Template sent to {} — id={}", phone, mid);
//             } catch (WebClientResponseException e) {
//                 String err = "Meta API " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
//                 entry.setStatus(WhatsAppLog.Status.FAILED);
//                 entry.setErrorMessage(err);
//                 results.add(Map.of("recipient", phone, "status", "FAILED", "error", err));
//             } catch (Exception e) {
//                 entry.setStatus(WhatsAppLog.Status.FAILED);
//                 entry.setErrorMessage(e.getMessage());
//                 results.add(Map.of("recipient", phone, "status", "FAILED", "error", e.getMessage()));
//             } finally {
//                 logRepository.save(entry);
//             }
//         }
//         return results;
//     }

//     // ─────────────────────────────────────────────────────────
//     // ← NEW: sendPdfAlert() — upload PDF and send as document
//     // ─────────────────────────────────────────────────────────

//     /**
//      * Send a PDF file as a WhatsApp document attachment to a list of recipients.
//      *
//      * Flow:
//      *   1. Upload the PDF to Meta's media endpoint → get mediaId
//      *   2. Send a document message with the mediaId + caption to each recipient
//      *
//      * @param recipients  list of phone numbers
//      * @param pdfBytes    the PDF file content
//      * @param fileName    filename shown to the recipient (e.g. "salary_report.pdf")
//      * @param caption     text shown above the document in WhatsApp
//      * @return per-recipient results
//      */
//     public List<Map<String, Object>> sendPdfAlert(List<String> recipients,
//                                                     byte[] pdfBytes,
//                                                     String fileName,
//                                                     String caption) {
//         List<Map<String, Object>> results = new ArrayList<>();

//         // Step 1: Upload the PDF once, reuse the mediaId for all recipients
//         String mediaId;
//         try {
//             mediaId = uploadMedia(pdfBytes, fileName);
//             log.info("PDF uploaded to Meta, mediaId={}", mediaId);
//         } catch (Exception e) {
//             log.error("Media upload failed: {}", e.getMessage());
//             // Fallback: send caption as text only
//             for (String phone : recipients) {
//                 results.add(Map.of(
//                     "recipient", sanitize(phone),
//                     "status",    "FAILED",
//                     "error",     "PDF upload failed: " + e.getMessage()
//                 ));
//             }
//             return results;
//         }

//         // Step 2: Send document message to each recipient
//         for (String raw : recipients) {
//             String phone = sanitize(raw);
//             WhatsAppLog entry = WhatsAppLog.builder()
//                     .recipient(phone)
//                     .messageBody("[PDF] " + fileName + " — " + caption)
//                     .status(WhatsAppLog.Status.PENDING)
//                     .build();
//             try {
//                 Map<String, Object> response = callDocumentApi(phone, mediaId, fileName, caption);
//                 String mid = extractId(response);
//                 entry.setStatus(WhatsAppLog.Status.SENT);
//                 entry.setWaMessageId(mid);
//                 results.add(Map.of(
//                     "recipient", phone, "status", "SENT",
//                     "messageId", mid, "fileName", fileName
//                 ));
//                 log.info("PDF sent to {} — id={}", phone, mid);
//             } catch (WebClientResponseException e) {
//                 String err = "Meta API " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
//                 entry.setStatus(WhatsAppLog.Status.FAILED);
//                 entry.setErrorMessage(err);
//                 results.add(Map.of("recipient", phone, "status", "FAILED", "error", err));
//             } catch (Exception e) {
//                 entry.setStatus(WhatsAppLog.Status.FAILED);
//                 entry.setErrorMessage(e.getMessage());
//                 results.add(Map.of("recipient", phone, "status", "FAILED", "error", e.getMessage()));
//             } finally {
//                 logRepository.save(entry);
//             }
//         }
//         return results;
//     }

//     // ─────────────────────────────────────────────────────────
//     // formatQueryResultAsMessage() — unchanged
//     // ─────────────────────────────────────────────────────────

//     public String formatQueryResultAsMessage(List<String> columns,
//                                               List<List<Object>> rows,
//                                               String header) {
//         StringBuilder sb = new StringBuilder();
//         sb.append("📊 *").append(header).append("*\n");
//         sb.append("━".repeat(28)).append("\n");
//         sb.append("*").append(String.join(" | ", columns)).append("*\n")
//           .append("─".repeat(28)).append("\n");
//         int limit = Math.min(rows.size(), 20);
//         for (int i = 0; i < limit; i++) {
//             List<String> vals = new ArrayList<>();
//             for (Object v : rows.get(i)) vals.add(v != null ? v.toString() : "-");
//             sb.append(String.join(" | ", vals)).append("\n");
//         }
//         if (rows.size() > 20) sb.append("_...and ").append(rows.size() - 20).append(" more rows_\n");
//         sb.append("━".repeat(28)).append("\n_Sent by WA Alert System_");
//         return sb.toString();
//     }

//     // ─────────────────────────────────────────────────────────
//     // Meta API calls
//     // ─────────────────────────────────────────────────────────

//     /** Send a plain text message. */
//     @SuppressWarnings("unchecked")
//     private Map<String, Object> callTextApi(String to, String body) {
//         Map<String, Object> payload = Map.of(
//             "messaging_product", "whatsapp",
//             "recipient_type",    "individual",
//             "to",                to,
//             "type",              "text",
//             "text",              Map.of("body", body, "preview_url", false)
//         );
//         return webClient.post()
//                 .uri("/{phoneNumberId}/messages", phoneNumberId)
//                 .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .bodyValue(payload)
//                 .retrieve()
//                 .bodyToMono(Map.class)
//                 .block();
//     }

//     /**
//      * Upload a PDF to Meta's media endpoint.
//      * Returns the mediaId to use in document messages.
//      *
//      * API: POST /{phone-number-id}/media
//      * Content-Type: multipart/form-data
//      * Fields: file (binary), type (application/pdf), messaging_product (whatsapp)
//      */
//     @SuppressWarnings("unchecked")
//     private String uploadMedia(byte[] pdfBytes, String fileName) {
//         MultipartBodyBuilder builder = new MultipartBodyBuilder();
//         builder.part("file", new ByteArrayResource(pdfBytes) {
//             @Override public String getFilename() { return fileName; }
//         }).contentType(MediaType.APPLICATION_PDF);
//         builder.part("type",               "application/pdf");
//         builder.part("messaging_product",  "whatsapp");

//         Map<String, Object> response = webClient.post()
//                 .uri("/{phoneNumberId}/media", phoneNumberId)
//                 .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
//                 .contentType(MediaType.MULTIPART_FORM_DATA)
//                 .bodyValue(builder.build())
//                 .retrieve()
//                 .bodyToMono(Map.class)
//                 .block();

//         if (response == null || !response.containsKey("id")) {
//             throw new RuntimeException("Media upload returned null or missing 'id'");
//         }
//         return response.get("id").toString();
//     }

//     /** Send a document (PDF) using an already-uploaded mediaId. */
//     @SuppressWarnings("unchecked")
//     private Map<String, Object> callDocumentApi(String to, String mediaId,
//                                                   String fileName, String caption) {
//         Map<String, Object> documentObj = new LinkedHashMap<>();
//         documentObj.put("id",       mediaId);
//         documentObj.put("filename", fileName);
//         documentObj.put("caption",  caption != null ? caption : "");

//         Map<String, Object> payload = new LinkedHashMap<>();
//         payload.put("messaging_product", "whatsapp");
//         payload.put("recipient_type",    "individual");
//         payload.put("to",                to);
//         payload.put("type",              "document");
//         payload.put("document",          documentObj);

//         return webClient.post()
//                 .uri("/{phoneNumberId}/messages", phoneNumberId)
//                 .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .bodyValue(payload)
//                 .retrieve()
//                 .bodyToMono(Map.class)
//                 .block();
//     }

//     // ─────────────────────────────────────────────────────────
//     // Template engine and helpers (unchanged)
//     // ─────────────────────────────────────────────────────────

//     private String fillTemplate(String template, Map<String, Object> row) {
//         if (template == null) return "";
//         Pattern p = Pattern.compile("\\{\\{(\\w+)}}");
//         Matcher m = p.matcher(template);
//         StringBuilder sb = new StringBuilder();
//         while (m.find()) {
//             String key = m.group(1);
//             String val = row.entrySet().stream()
//                     .filter(e -> e.getKey().equalsIgnoreCase(key))
//                     .map(e -> e.getValue() != null ? e.getValue().toString() : "")
//                     .findFirst()
//                     .orElse("{{" + key + "}}");
//             m.appendReplacement(sb, Matcher.quoteReplacement(val));
//         }
//         m.appendTail(sb);
//         return sb.toString();
//     }

//     private String resolvePhone(Map<String, Object> row, String phoneKey) {
//         if (phoneKey != null && !phoneKey.isBlank()) {
//             Object v = row.entrySet().stream()
//                     .filter(e -> e.getKey().equalsIgnoreCase(phoneKey))
//                     .map(Map.Entry::getValue).findFirst().orElse(null);
//             return v != null ? v.toString() : null;
//         }
//         for (String alias : PHONE_KEYS) {
//             for (Map.Entry<String, Object> e : row.entrySet()) {
//                 if (e.getKey().equalsIgnoreCase(alias) && e.getValue() != null) {
//                     return e.getValue().toString();
//                 }
//             }
//         }
//         return null;
//     }

//     @SuppressWarnings("unchecked")
//     private String extractId(Map<String, Object> response) {
//         if (response == null) return "unknown";
//         List<Map<String, Object>> messages = (List<Map<String, Object>>) response.get("messages");
//         if (messages != null && !messages.isEmpty()) {
//             Object id = messages.get(0).get("id");
//             return id != null ? id.toString() : "unknown";
//         }
//         return "unknown";
//     }

//     private String sanitize(String phone) {
//         return phone.replaceAll("[\\s\\-()]+", "").replaceAll("^\\+", "");
//     }
// }
package com.waalert.whatsapp_alert_backend.service;

import com.waalert.whatsapp_alert_backend.dto.TemplateSendRequest;
import com.waalert.whatsapp_alert_backend.dto.WhatsAppRequest;
import com.waalert.whatsapp_alert_backend.entity.WhatsAppLog;
import com.waalert.whatsapp_alert_backend.repository.WhatsAppLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class WhatsAppService {

    private final WhatsAppLogRepository logRepository;
    private final WebClient webClient;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    private static final List<String> PHONE_KEYS = List.of(
        "mob_no", "mobile", "phone", "whatsapp_number", "mobile_no",
        "contact", "phone_no", "cell", "cell_no", "contact_no"
    );

    public WhatsAppService(WhatsAppLogRepository logRepository,
                           @Qualifier("whatsAppWebClient") WebClient webClient) {
        this.logRepository = logRepository;
        this.webClient     = webClient;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Existing: send plain text messages
    // ─────────────────────────────────────────────────────────────────────────

    public List<Map<String, Object>> sendMessages(WhatsAppRequest request) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (String raw : request.getRecipients()) {
            String phone = sanitize(raw);
            WhatsAppLog entry = WhatsAppLog.builder()
                    .recipient(phone).messageBody(request.getMessage())
                    .status(WhatsAppLog.Status.PENDING).build();
            try {
                Map<String, Object> response = callTextApi(buildTextPayload(phone, request.getMessage()));
                String mid = extractId(response);
                entry.setStatus(WhatsAppLog.Status.SENT);
                entry.setWaMessageId(mid);
                results.add(Map.of("recipient", phone, "status", "SENT", "messageId", mid));
                log.info("WhatsApp sent to {} — id={}", phone, mid);
            } catch (WebClientResponseException e) {
                String err = "Meta API " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
                entry.setStatus(WhatsAppLog.Status.FAILED);
                entry.setErrorMessage(err);
                results.add(Map.of("recipient", phone, "status", "FAILED", "error", err));
                log.error("WhatsApp failed for {}: {}", phone, err);
            } catch (Exception e) {
                entry.setStatus(WhatsAppLog.Status.FAILED);
                entry.setErrorMessage(e.getMessage());
                results.add(Map.of("recipient", phone, "status", "FAILED", "error", e.getMessage()));
            } finally {
                logRepository.save(entry);
            }
        }
        return results;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Existing: template-based per-row messaging
    // ─────────────────────────────────────────────────────────────────────────

    public List<Map<String, Object>> sendTemplate(TemplateSendRequest request) {
        List<Map<String, Object>> results = new ArrayList<>();
        if (request.getRows() == null || request.getRows().isEmpty()) return results;

        for (Map<String, Object> row : request.getRows()) {
            String phone = resolvePhone(row, request.getPhoneKey());
            if (phone == null || phone.isBlank()) {
                results.add(Map.of("status", "SKIPPED", "reason", "No phone number in row", "row", row));
                continue;
            }
            phone = sanitize(phone);
            String message = fillTemplate(request.getTemplate(), row);

            WhatsAppLog entry = WhatsAppLog.builder()
                    .recipient(phone).messageBody(message)
                    .status(WhatsAppLog.Status.PENDING).build();
            try {
                Map<String, Object> response = callTextApi(buildTextPayload(phone, message));
                String mid = extractId(response);
                entry.setStatus(WhatsAppLog.Status.SENT);
                entry.setWaMessageId(mid);
                results.add(Map.of("recipient", phone, "status", "SENT", "messageId", mid, "message", message));
            } catch (WebClientResponseException e) {
                String err = "Meta API " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
                entry.setStatus(WhatsAppLog.Status.FAILED);
                entry.setErrorMessage(err);
                results.add(Map.of("recipient", phone, "status", "FAILED", "error", err));
            } catch (Exception e) {
                entry.setStatus(WhatsAppLog.Status.FAILED);
                entry.setErrorMessage(e.getMessage());
                results.add(Map.of("recipient", phone, "status", "FAILED", "error", e.getMessage()));
            } finally {
                logRepository.save(entry);
            }
        }
        return results;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ✅ NEW: Send a PDF document via WhatsApp Cloud API
    //
    //  Step 1 — Upload PDF → Meta media endpoint → get media_id
    //  Step 2 — Send document message using media_id
    //
    // @param phone      recipient phone number (digits only, no +)
    // @param caption    text caption shown below the document
    // @param filename   filename shown in WhatsApp (e.g. report_20241021_120000.pdf)
    // @param pdfBytes   raw PDF byte array
    // @return           result map with status and messageId
    // ─────────────────────────────────────────────────────────────────────────

    public Map<String, Object> sendPdfDocument(String phone, String caption,
                                                String filename, byte[] pdfBytes) {
        phone = sanitize(phone);

        WhatsAppLog entry = WhatsAppLog.builder()
                .recipient(phone).messageBody(caption)
                .status(WhatsAppLog.Status.PENDING).build();

        try {
            // Step 1 — Upload the PDF to Meta's media endpoint
            String mediaId = uploadMedia(pdfBytes, filename);
            log.info("PDF uploaded to Meta, mediaId={}", mediaId);

            // Step 2 — Send the document message
            Map<String, Object> docPayload = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type",    "individual",
                "to",                phone,
                "type",              "document",
                "document",          Map.of(
                    "id",       mediaId,
                    "caption",  caption,
                    "filename", filename
                )
            );

            Map<String, Object> response = callTextApi(docPayload);
            String mid = extractId(response);
            entry.setStatus(WhatsAppLog.Status.SENT);
            entry.setWaMessageId(mid);
            logRepository.save(entry);

            log.info("PDF document sent to {} — messageId={}", phone, mid);
            return Map.of("recipient", phone, "status", "SENT", "messageId", mid);

        } catch (WebClientResponseException e) {
            String err = "Meta API " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
            entry.setStatus(WhatsAppLog.Status.FAILED);
            entry.setErrorMessage(err);
            logRepository.save(entry);
            log.error("PDF send failed for {}: {}", phone, err);
            return Map.of("recipient", phone, "status", "FAILED", "error", err);
        } catch (Exception e) {
            entry.setStatus(WhatsAppLog.Status.FAILED);
            entry.setErrorMessage(e.getMessage());
            logRepository.save(entry);
            return Map.of("recipient", phone, "status", "FAILED", "error", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Upload PDF bytes to Meta's media API.
     * Returns the media_id string needed for sending a document message.
     */
    @SuppressWarnings("unchecked")
    private String uploadMedia(byte[] pdfBytes, String filename) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("messaging_product", "whatsapp");
        body.add("type", "application/pdf");
        body.add("file", new ByteArrayResource(pdfBytes) {
            @Override public String getFilename() { return filename; }
        });

        Map<String, Object> response = webClient.post()
                .uri("/{phoneNumberId}/media", phoneNumberId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("id")) {
            throw new RuntimeException("Media upload failed — no id in response: " + response);
        }
        return response.get("id").toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callTextApi(Map<String, Object> payload) {
        return webClient.post()
                .uri("/{phoneNumberId}/messages", phoneNumberId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    private Map<String, Object> buildTextPayload(String to, String body) {
        return Map.of(
            "messaging_product", "whatsapp",
            "recipient_type",    "individual",
            "to",                to,
            "type",              "text",
            "text",              Map.of("body", body, "preview_url", false)
        );
    }

    @SuppressWarnings("unchecked")
    private String extractId(Map<String, Object> response) {
        if (response == null) return "unknown";
        List<Map<String, Object>> messages = (List<Map<String, Object>>) response.get("messages");
        if (messages != null && !messages.isEmpty()) {
            Object id = messages.get(0).get("id");
            return id != null ? id.toString() : "unknown";
        }
        return "unknown";
    }

    private String fillTemplate(String template, Map<String, Object> row) {
        if (template == null) return "";
        Pattern p = Pattern.compile("\\{\\{(\\w+)}}");
        Matcher m = p.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String key   = m.group(1);
            String value = row.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(key))
                    .map(e -> e.getValue() != null ? e.getValue().toString() : "")
                    .findFirst().orElse("{{" + key + "}}");
            m.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String resolvePhone(Map<String, Object> row, String phoneKey) {
        if (phoneKey != null && !phoneKey.isBlank()) {
            Object v = row.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(phoneKey))
                    .map(Map.Entry::getValue).findFirst().orElse(null);
            return v != null ? v.toString() : null;
        }
        for (String alias : PHONE_KEYS) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(alias) && entry.getValue() != null)
                    return entry.getValue().toString();
            }
        }
        return null;
    }

    private String sanitize(String phone) {
        return phone.replaceAll("[\\s\\-()]+", "").replaceAll("^\\+", "");
    }

    public String formatQueryResultAsMessage(List<String> columns, List<List<Object>> rows, String header) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 *").append(header).append("*\n");
        sb.append("━".repeat(28)).append("\n");
        sb.append("*").append(String.join(" | ", columns)).append("*\n").append("─".repeat(28)).append("\n");
        int limit = Math.min(rows.size(), 20);
        for (int i = 0; i < limit; i++) {
            List<String> vals = new ArrayList<>();
            for (Object v : rows.get(i)) vals.add(v != null ? v.toString() : "-");
            sb.append(String.join(" | ", vals)).append("\n");
        }
        if (rows.size() > 20) sb.append("_...and ").append(rows.size() - 20).append(" more rows_\n");
        sb.append("━".repeat(28)).append("\n_Sent by Smart Alert System_");
        return sb.toString();
    }
}