package teamssavice.ssavice.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.chat.entity.ChatMessageEntity;
import teamssavice.ssavice.chat.infrastructure.repository.ChatMessageRepository;
import teamssavice.ssavice.kafka.event.KafkaEvent;

@Service
@RequiredArgsConstructor
public class ChatWriteService {
    private final ChatMessageRepository chatMessageRepository;

    public Mono<Void> save(KafkaEvent.Save event) {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .messageType(event.messageType())
                .roomType(event.roomType())
                .roomId(event.roomId())
                .receiver(event.receiver())
                .sender(event.sender())
                .message(event.message())
                .serviceId(event.serviceId())
                .createdAt(event.createdAt())
                .build();

        return chatMessageRepository.save(entity)
                .then();
    }
}
