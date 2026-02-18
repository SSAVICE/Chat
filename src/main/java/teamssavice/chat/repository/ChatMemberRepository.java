package teamssavice.chat.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import teamssavice.chat.model.ChatMember;

public interface ChatMemberRepository extends R2dbcRepository<ChatMember, Long> {

}
