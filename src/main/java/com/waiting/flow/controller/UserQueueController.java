package com.waiting.flow.controller;

import com.waiting.flow.dto.AllowUserResponse;
import com.waiting.flow.dto.AllowedUserResponse;
import com.waiting.flow.dto.RankNumberResponse;
import com.waiting.flow.dto.RegisterUserResponse;
import com.waiting.flow.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class UserQueueController {

    private final UserQueueService userQueueService;

    @PostMapping("")
    public Mono<RegisterUserResponse> registerUser( @RequestParam(name = "queue", defaultValue = "default") String queue,
                                                    @RequestParam(name = "user_id") Long userId){
        return  userQueueService.registerWaitQueue(queue,userId)
                .map(RegisterUserResponse::new);
    }

    @PostMapping("/allow")
    public Mono<AllowUserResponse> allowUser(@RequestParam(name = "queue" , defaultValue = "default") String queue,
                                             @RequestParam(name = "count") Long count) {

        return userQueueService.allowUser(queue,count)
                .map(allowed -> new AllowUserResponse(count, allowed));
    }

    @GetMapping("/allowed")
    public Mono<?> isAllowedUser( @RequestParam(name = "queue", defaultValue = "default") String queue,
                                  @RequestParam(name = "user_id") Long userId){
        return userQueueService.isAllowed(queue, userId)
                .map(AllowedUserResponse::new);
    }

    @GetMapping("/rank")
    public Mono<RankNumberResponse> getRankUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                                 @RequestParam(name = "user_id") Long userId){
        return userQueueService.getRank(queue,userId)
                .map(RankNumberResponse::new);
    }

}
