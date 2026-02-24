package teamssavice.ssavice.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.chat.controller.dto.ChatRequest;
import teamssavice.ssavice.chat.controller.dto.ChatResponse;
import teamssavice.ssavice.chat.service.ChatService;
import teamssavice.ssavice.global.annotation.CurrentAuth;
import teamssavice.ssavice.global.dto.Auth;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Validated
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/messages")
    public Mono<ResponseEntity<ChatResponse.Messages>> getMessagesByCursor(
            @CurrentAuth Auth auth,
            @Valid @ModelAttribute ChatRequest.MessageCursor request
    ) {
        return chatService.getMessagesByCursor(auth, request.toCommand())
                .collectList()
                .map(ChatResponse.Messages::from)
                .map(ResponseEntity::ok);
    }
}
