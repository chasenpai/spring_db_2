package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    @Transactional
    public void joinV1(String username){

        Member member = new Member(username);
        Log logMsg = new Log(username);

        log.info("-- memberRepository 호출 시작 --");
        memberRepository.save(member);
        log.info("-- memberRepository 호출 종료 --");

        log.info("-- logRepository 호출 시작 --");
        logRepository.save(logMsg);
        log.info("-- logRepository 호출 시작 --");
    }

    @Transactional
    public void joinV2(String username){

        Member member = new Member(username);
        Log logMsg = new Log(username);

        log.info("-- memberRepository 호출 시작 --");
        memberRepository.save(member);
        log.info("-- memberRepository 호출 종료 --");

        log.info("-- logRepository 호출 시작 --");
        try{
            logRepository.save(logMsg);
        }catch (RuntimeException e){
            log.info("log 저장에 실패했습니다. = {}", logMsg.getMsg());
        }
        log.info("-- logRepository 호출 시작 --");
    }

}
