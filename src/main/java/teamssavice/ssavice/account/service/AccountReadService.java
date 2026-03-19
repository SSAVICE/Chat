package teamssavice.ssavice.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.account.AccountInfoDto;
import teamssavice.ssavice.account.infrastructure.repository.AccountRepository;
import teamssavice.ssavice.room.RoomType;
import teamssavice.ssavice.room.entity.RoomEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountReadService {

    private final AccountRepository accountRepository;

    public Mono<Map<String, AccountInfoDto>> getNameMap(List<RoomEntity> rooms, Long mySubject) {
        List<Long> oppSubjects = rooms.stream()
                .filter(room -> RoomType.DM.equals(room.getType()))
                .map(room -> parseOpponentSubject(room.getRoomName(), mySubject))
                .distinct()
                .toList();

        if(oppSubjects.isEmpty()) return Mono.just(Collections.emptyMap());

        return findAccountBySubjectIn(oppSubjects)
                .map(dtos -> {
                    Map<Long, AccountInfoDto> subjectToDto = dtos.stream()
                            .collect(Collectors.toMap(
                                    AccountInfoDto::getAccountId,
                                    dto -> dto
                            ));

                    return rooms.stream()
                            .filter(room -> RoomType.DM.equals(room.getType()))
                            .collect(Collectors.toMap(
                                    RoomEntity::getRoomName,
                                    room -> {
                                        Long oppSubject = parseOpponentSubject(room.getRoomName(), mySubject);
                                        return subjectToDto.getOrDefault(oppSubject, AccountInfoDto.builder().userName("알 수 없음").build());
                                    }
                            ));
                });
    }

    public Mono<List<AccountInfoDto>> findAccountBySubjectIn(List<Long> subjects) {
        return subjects.isEmpty()
                ? Mono.just(Collections.emptyList())
                : accountRepository.findAccountInfoDtoBySubjectIn(subjects)
                .collectList();
    }

    public Mono<String> getOppName(String roomId, Long mySubject) {
        long oppSubject = parseOpponentSubject(roomId, mySubject);
        return accountRepository.findAccountInfoDtoBySubject(oppSubject)
                .map(AccountInfoDto::getName);
    }

    private Long parseOpponentSubject(String roomName, Long mySubject) {
        String[] parts = roomName.split("_");
        long subjectA = Long.parseLong(parts[0]);
        long subjectB = Long.parseLong(parts[1]);
        return mySubject.equals(subjectA) ? subjectB : subjectA;
    }
}
