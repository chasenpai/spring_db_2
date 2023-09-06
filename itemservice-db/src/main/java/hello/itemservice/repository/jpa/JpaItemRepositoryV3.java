package hello.itemservice.repository.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static hello.itemservice.domain.QItem.item;

@Repository
@Transactional
@RequiredArgsConstructor
public class JpaItemRepositoryV3 implements ItemRepository {

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory; //Querydsl 을 사용하기 위해 필요하다. 스프링 빈으로 등록해서 사용해도 됨

    public JpaItemRepositoryV3(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager); //JPQL 을 만들기 때문에 EntityManager 필요
    }

    @Override
    public Item save(Item item) {
        entityManager.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = findById(itemId).orElseThrow();
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = entityManager.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    public List<Item> findAllOld(ItemSearchCond cond) {

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        BooleanBuilder builder = new BooleanBuilder(); //BooleanBuilder 조건 생성 - 동적쿼리를 깔끔하게 작성 가능

        //만약 쿼리에 오류가 있다면 컴파일 시점에 잡을 수 있다
        if (StringUtils.hasText(itemName)) {
            builder.and(item.itemName.like("%" + itemName + "%"));
        }
        if (maxPrice != null) {
            builder.and(item.price.loe(maxPrice)); //loe(<=)
        }

        return queryFactory.select(item) //Q 클래스 static import
                .from(item)
                .where(builder)
                .fetch();
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        return queryFactory
                .select(item)
                .from(item)
                .where(
                        //where 절에 다양한 조건을 넣으면 AND 조건으로 처리되고, null 일 경우 해당 조건을 무시한다
                        likeItemName(itemName), maxPrice(maxPrice)
                )
                .fetch();
    }

    private BooleanExpression likeItemName(String itemName) {
        if (StringUtils.hasText(itemName)) {
            return item.itemName.like("%" + itemName + "%");
        }
        return null;
    }

    private BooleanExpression maxPrice(Integer maxPrice) {
        if (maxPrice != null) {
            return item.price.loe(maxPrice);
        }
        return null;
    }
}
