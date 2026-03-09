package teamssavice.ssavice.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.chat.entity.ChatMessageEntity;
import teamssavice.ssavice.chat.infrastructure.repository.ChatMessageRepository;
import teamssavice.ssavice.chat.service.dto.ChatCommand;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatWriteService {
    private final ChatMessageRepository chatMessageRepository;

    public Mono<Void> save(ChatCommand.Chat command) {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .messageId(command.messageId())
                .messageType(command.messageType())
                .roomType(command.roomType())
                .roomId(command.roomId())
                .receiver(command.receiver())
                .sender(command.sender())
                .message(command.message())
                .createdAt(command.createdAt())
                .serviceId(command.serviceId())
                .build();

        return chatMessageRepository.save(entity)
                .doOnError(e -> log.error("메시지 저장 실패: {}", e.getMessage(), e))
                .then();
    }
}
