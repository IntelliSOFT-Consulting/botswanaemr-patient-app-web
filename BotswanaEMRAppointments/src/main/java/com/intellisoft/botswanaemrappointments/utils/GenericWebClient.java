package com.intellisoft.botswanaemrappointments.utils;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellisoft.botswanaemrappointments.utils.exception.CustomTimeoutException;
import com.intellisoft.botswanaemrappointments.utils.exception.ServerException;
import com.intellisoft.botswanaemrappointments.utils.exception.ServiceException;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Log4j2
public class GenericWebClient {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private static HttpHeaders createHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of((MediaType.APPLICATION_JSON)));
        headers.set("Authorization", authToken);
        return headers;
    }
    
    /**
     * Extracts a user-friendly error message from OpenMRS error response JSON
     * @param errorBody The JSON error response body
     * @return A cleaned error message
     */
    private static String extractErrorMessage(String errorBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(errorBody);
            JsonNode errorNode = rootNode.get("error");
            if (errorNode != null) {
                // Check for specific OpenMRS bugs
                JsonNode detailNode = errorNode.get("detail");
                if (detailNode != null) {
                    String detail = detailNode.asText();
                    // Check for the known NullPointerException bug in AppointmentResource1_9.save:132
                    if (detail.contains("NullPointerException") && detail.contains("AppointmentResource1_9:132")) {
                        return "Unable to cancel appointment due to a system limitation. This is a known issue with appointments that have associated visits.";
                    }
                    // Check for PropertyValueException - missing appointment status history in database
                    if (detail.contains("PropertyValueException") && detail.contains("PatientAppointment.status")) {
                        return "Unable to cancel appointment due to missing status history in the database. This is a data integrity issue in the appointment system.";
                    }
                }
                
                JsonNode messageNode = errorNode.get("message");
                if (messageNode != null && !messageNode.asText().equals("[null]") && !messageNode.asText().isEmpty()) {
                    return messageNode.asText();
                }
                // If message is null or empty, try to extract from detail
                if (detailNode != null) {
                    String detail = detailNode.asText();
                    // Extract the exception type (e.g., "NullPointerException")
                    if (detail.contains("Exception")) {
                        int exceptionIndex = detail.indexOf("Exception");
                        if (exceptionIndex > 0) {
                            int startIndex = detail.lastIndexOf(".", exceptionIndex);
                            if (startIndex < 0) startIndex = detail.lastIndexOf(" ", exceptionIndex);
                            if (startIndex >= 0) {
                                String exceptionType = detail.substring(startIndex + 1, exceptionIndex + 9);
                                return "An error occurred: " + exceptionType;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse error response JSON: {}", errorBody, e);
        }
        // Fallback to generic message
        return "The external service encountered an error";
    }

    public static <T, V> T postRequest(WebClient webClient, String url, String token, V req, Class<T> clazz) {
        log.info("URL: {}", url);
        return webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(createHeaders(token)))
                .bodyValue(req)
                .retrieve()
                .onStatus(
                        HttpStatus::is4xxClientError,
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            log.error("4xx Client Error - URL: {}, Status: {}, Body: {}", url, response.rawStatusCode(), body);
                            return Mono.error(new ServiceException(body, response.rawStatusCode()));
                        }))
                .onStatus(
                        HttpStatus::is5xxServerError,
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            log.error("5xx Server Error - URL: {}, Status: {}, Body: {}", url, response.rawStatusCode(), body);
                            String errorMessage = extractErrorMessage(body);
                            return Mono.error(new ServerException(errorMessage));
                        }))
                .bodyToMono(clazz)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof ReadTimeoutException))
                .doOnError(ReadTimeoutException.class, e -> {//change it to io.netty.handler.timeout.ReadTimeoutException
                    log.error("Timeout error for URL: {}", url, e);
                    throw new CustomTimeoutException(e.getMessage());
                })
                .doOnError(ServerException.class, e -> {
                    log.error("Server error for URL: {}", url, e);
                    throw e;
                })
                .doOnError(ServiceException.class, e -> {
                    log.error("Service error for URL: {}", url, e);
                    throw e;
                })
                .block();
    }

    public static <T> T getRequest(WebClient webClient, String url, String token, Class<T> clazz) {
        log.info("URL: {}", url);
        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(createHeaders(token)))
                .retrieve()
                .onStatus(
                        HttpStatus::is4xxClientError,
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            log.info("Body is {}", body);
                            return Mono.error(new ServiceException(body, response.rawStatusCode()));
                        }))
                .bodyToMono(clazz)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof ReadTimeoutException))
                .doOnError(ReadTimeoutException.class, e -> {//change it to io.netty.handler.timeout.ReadTimeoutException
                    log.error(e);
                    throw new CustomTimeoutException(e.getMessage());
                })
                .doOnError(IllegalStateException.class, e -> {throw new ServerException("An error occurred.");
                })
                .block();
    }

    public static <T, V> T putRequest(WebClient webClient, String url, String token, V req, Class<T> clazz) {
        log.info("URL: {}", url);
        return webClient.put()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(createHeaders(token)))
                .bodyValue(req)
                .retrieve()
                .onStatus(
                        HttpStatus::is4xxClientError,
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            log.info("Body is {}", body);
                            return Mono.error(new ServiceException(body, response.rawStatusCode()));
                        }))
                .bodyToMono(clazz)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof ReadTimeoutException))
                .doOnError(ReadTimeoutException.class, e -> {//change it to io.netty.handler.timeout.ReadTimeoutException
                    log.error(e);
                    throw new CustomTimeoutException(e.getMessage());
                })
                .block();
    }

}
