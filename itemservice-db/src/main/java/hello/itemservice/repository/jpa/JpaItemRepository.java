package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
/**
 * JPA 예외 변환
 * - @Repository 가 붙은 클래스는 컴포넌트 스캔의 대상이 되고, 예외 변환 AOP 의 적용 대상이 된다
 * - 스프링은 JPA 예외 변환기 PersistenceExceptionTranslator 를 등록하고 예외를 변환 시켜준다
 */
@Transactional //JPA 에서 데이터 변경시 트랜잭션은 필수. 이것은 예시이고 서비스 계층에 걸어주는게 맞다
public class JpaItemRepository implements ItemRepository {

    /**
     * 엔티티 매니저
     *  - JPA 의 모든 동작은 엔티티 매니저를 통해 이루어진다
     *  - 내부에 데이터소스를 가지고 있고 데이터베이스에 접근할 수 있다
     *  - JPA 를 설정하려면 EntityManagerFactory, JPA TransactionManager 등 다양한 설정을 해줘야 한다
     *  - 하지만 스프링 부트는 다 자동으로 해준다
     */
    private final EntityManager entityManager;

    public JpaItemRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Item save(Item item) {
        entityManager.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = entityManager.find(Item.class, itemId);
        //JPA 는 트랜잭션이 커밋되는 시점에 변경된 엔티티 객체가 있는지 확인하고 update query 를 실행한다
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item =  entityManager.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {

        /**
         * 객체지향 쿼리 언어 JPQL
         * - SQL 이 테이블을 대상으로 한다면 JPQL 은 엔티티 객체를 대상으로 SQL 을 실행한다
         * - 엔티티 객체를 대상으로 하기 때문에 엔티티 객체와 속성의 대소문자 구분해야 한다
         */
        String jpql = "select i from Item i";

        Integer maxPrice = cond.getMaxPrice();
        String itemName = cond.getItemName();

        if (StringUtils.hasText(itemName) || maxPrice != null) {
            jpql += " where";
        }

        boolean andFlag = false;

        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName like concat('%',:itemName,'%')";
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                jpql += " and";
            }
            jpql += " i.price <= :maxPrice";
        }
        log.info("jpql={}", jpql);

        TypedQuery<Item> query = entityManager.createQuery(jpql, Item.class);

        if (StringUtils.hasText(itemName)) {
            query.setParameter("itemName", itemName);
        }

        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }

        return query.getResultList();
    }

}
