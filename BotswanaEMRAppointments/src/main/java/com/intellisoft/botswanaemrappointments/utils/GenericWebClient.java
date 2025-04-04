package com.intellisoft.botswanaemrappointments.utils;


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
    private static HttpHeaders createHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of((MediaType.APPLICATION_JSON)));
        headers.set("Authorization", authToken);
        return headers;
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


}
