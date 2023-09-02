package hello.itemservice.repository.mybatis;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper //MyBatis 에서 인식
public interface ItemMapper {

    /**
     * ItemMapper 인터페이스의 동작 원리
     * 1. 애플리케이션 로딩 시점에 MyBatis 스프링 연동 모듈이 @Mapper 가 붙어있는 인터페이스를 조사한다
     * 2. 해당 인터페이스가 발견되면 동적 프록시 기술을 사용해서 ItemMapper 인터페이스 구현체를 만든다
     * 3. 생성된 구현체를 스프링 빈으로 등록한다
     */
    void save(Item item);

    void update(@Param("id") Long id, @Param("updateParam") ItemUpdateDto updateParam);

    Optional<Item> findById(Long id);

    List<Item> findAll(ItemSearchCond itemSearch);

}
