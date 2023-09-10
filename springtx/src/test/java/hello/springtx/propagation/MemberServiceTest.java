package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /**
     * 서비스 계층에 트랜잭션이 없을 때 커밋
     * MemberService - 트랜잭션 X
     * MemberRepository - 트랜잭션 O
     * LogRepository - 트랜잭션 O
     */
    @Test
    void outerTxOffSuccess(){

        String username = "outerTxOffSuccess";

        memberService.joinV1(username);

        //모든 데이터 정상 저장
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * 서비스 계층에 트랜잭션이 없을 때 롤백
     * MemberService - 트랜잭션 X
     * MemberRepository - 트랜잭션 O
     * LogRepository - 트랜잭션 O + 예외발생
     */
    @Test
    void outerTxOffFailed(){

        String username = "로그예외 outerTxOffFailed";

        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        //완전히 롤백되지 않고, Member 만 저장
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * 단일 트랜잭션
     * MemberService - 트랜잭션 O
     * MemberRepository - 트랜잭션 X
     * LogRepository - 트랜잭션 X
     */
    @Test
    void singleTx(){

        String username = "singleTx";

        memberService.joinV1(username);

        //모든 데이터 정상 저장
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * 모든 논리 트랜잭션이 정상 커밋되는 경우
     * MemberService - 트랜잭션 O
     * MemberRepository - 트랜잭션 O
     * LogRepository - 트랜잭션 O
     */
    @Test
    void outerTxOnSuccess(){

        String username = "outerTxOnSuccess";

        memberService.joinV1(username);

        //모든 데이터 정상 저장
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * 논리 트랜잭션 예외 발생으로 롤백되는 경우
     * MemberService - 트랜잭션 O
     * MemberRepository - 트랜잭션 O
     * LogRepository - 트랜잭션 O + 예외발생
     */
    @Test
    void outerTxOnFailed(){

        String username = "로그예외 outerTxOnFailed";

        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        //모두 롤백
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

}