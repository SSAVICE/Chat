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
                .roomType(RoomType.GROUP)
                .roomId("testroomIdIDId")
                .roomName("testroomIdIDId")
                .sender(1L)
                .createdAt(LocalDateTime.now())
                .build();
        kafkaProducer.publish("testroomIdIDId", event).subscribe();
    }

    @GetMapping("/leave")
    public void leaveTest() {
        KafkaEvent.Join event = KafkaEvent.Join.builder()
                .messageType(MessageType.LEAVE)
                .roomType(RoomType.GROUP)
                .roomId("testroomIdIDId")
                .roomName("testroomIdIDId")
                .sender(1L)
                .createdAt(LocalDateTime.now())
                .build();
        kafkaProducer.publish("testroomIdIDId", event).subscribe();
    }
}
