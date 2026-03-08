package teamssavice.ssavice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import teamssavice.ssavice.chat.MessageType;
import teamssavice.ssavice.kafka.event.KafkaEvent;
import teamssavice.ssavice.room.RoomType;

import java.time.LocalDateTime;

@RestController
@RequestMapping("api/test")
@RequiredArgsConstructor
public class TestController {
    private final KafkaProducer kafkaProducer;

    @GetMapping("/join")
    public void joinTest() {
        KafkaEvent.Join event = KafkaEvent.Join.builder()
                .messageType(MessageType.JOIN)
                .roomType(RoomType.DM)
                .roomId("1_2")
                .roomName("1_2")
                .sender(1L)
                .createdAt(LocalDateTime.now())
                .build();
        kafkaProducer.publish("1_2", event).subscribe();
    }

    @GetMapping("/leave")
    public void leaveTest() {
        KafkaEvent.Join event = KafkaEvent.Join.builder()
                .messageType(MessageType.LEAVE)
                .roomType(RoomType.DM)
                .roomId("1_2")
                .roomName("1_2")
                .sender(1L)
                .createdAt(LocalDateTime.now())
                .build();
        kafkaProducer.publish("1_2", event).subscribe();
    }
}
