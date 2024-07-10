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
    private final String USER_QUEUE_PROCEED_KEY = "users:queue:%s:proceed";


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

    // 진입을 허용
    public Mono<?> allowUser(final String queue, final Long count){
        // 진입을 허용하는 단계
        // 1.wait queue 사용자를 제거
        // 2. proceed queue 사용자를 추가
        return reactiveRedisTemplate.opsForZSet().popMin(USER_QUEUE_WAIT_KEY.formatted(queue),count)
                .flatMap( member -> reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_PROCEED_KEY.formatted(queue), member.getValue(), Instant.now().getEpochSecond() ))
                .count();
    }


    // 진입이 가능한 상태인지 조회
    public Mono<Boolean> isAllowed(final String queue, final Long userId){
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_PROCEED_KEY.formatted(queue),userId.toString())
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0);
    }

}
