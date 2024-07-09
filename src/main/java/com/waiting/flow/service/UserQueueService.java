package com.waiting.flow.service;

import com.waiting.flow.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserQueueService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private final String USER_QUEUE_WAIT_KEY="user-queue:%s:wait";


    // 대기열 등록 API
    public Mono<Long> registerWaitQueue(final String queue, final Long userId){
        // redis의 Sorted Set을 사용
        // Key: userId
        // Value : unix TimeStamp
        // rank
        var unixTimeStamp = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queue),userId.toString(),unixTimeStamp)
                .filter(i -> i) // true인 경우
                .switchIfEmpty(Mono.error(ErrorCode.QUEUE_ALREADY_REGISTERED_USER.build())) // False일 때 Error
                .flatMap(i -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString())) //
                .map(i -> i >= 0  ?  i + 1: i );
    }

}
