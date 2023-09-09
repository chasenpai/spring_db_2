package hello.springtx.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * 예외와 트랜잭션 커밋, 롤백
     * - 스프링은 체크 예외는 커밋하고, 언체크(런타임) 예외는 롤백을 한다
     * - 스프링은 기본적으로 체크 예외는 비즈니스 의미가 있을 때 사용하고, 런타임 예외는 복구 불가능한 예외로 가정한다
     * - 이런 정책을 반드시 따를 필요는 없고, rollbackFor 설정으로 체크 예외도 롤백하게 할 수 있다
     */
    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {

        log.info("order 호출");
        orderRepository.save(order);

        log.info("결제 프로세스 진입");

        if(order.getUsername().equals("예외")){
            log.info("시스템 예외 발생");
            throw new RuntimeException("시스템 예외");
        }else if(order.getUsername().equals("잔고부족")){
            /**
             * 만약 주문&결제 프로세스에서 고객의 잔고가 부족해서 예외가 발생한다면
             * 시스템에 문제가 있어서 발생하는 예외가 아니다. 시스템은 정상 동작했지만
             * 비즈니스 상황에서 문제가 되기 때문에 발생한 것으로 비즈니스 상황이 예외인 것이다
             * 이런 상황에서 고객에게 잔고 부족을 알리고 별도의 계좌로 입금하도록 안내할 경우
             * 롤백을 해버린다면 Order 자체가 사라지기 때문에 문제가 된다
             */
            log.info("잔고 부족 비즈니스 예외 발생");
            order.setPayStatus("대기");
            throw new NotEnoughMoneyException("잔고가 부족합니다.");
        }else{
            log.info("정상 승인");
            order.setPayStatus("완료");
        }

        log.info("결제 프로세스 완료");
    }

}
