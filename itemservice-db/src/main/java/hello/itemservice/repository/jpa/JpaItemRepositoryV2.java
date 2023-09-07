package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
@RequiredArgsConstructor
public class JpaItemRepositoryV2 implements ItemRepository {

    /**
     * 의존관계와 구조
     * - ItemService 는 ItemRepository 에 의존하기 때문에 SpringDataJpaItemRepository 를 그대로 사용할 수 없다
     * - ItemService 가 SpringDataJpaItemRepository 를 직접 사용하도록 코드를 고치면 된다
     * - 하지만 기존 코드의 변경 없이 DI 를 통해 구현 기술을 변경하는 것이 좋다
     * - JpaItemRepositoryV2 가 ItemRepository 와 SpringDataJpaItemRepository 사이를 맞추기 위한 어댑터 처럼 사용된다
     *
     * 하지만?
     * - 구조를 맞추기 위해서 중간에 어댑터가 들어가게 되니 전체 구조가 복잡해지고 사용하는 클래스가 많아졌다
     * - 유지보수 관점에서 ItemService 를 변경하지 않고 ItemRepository 의 구현체를 변경할 수 있는 장점이 있지만
     * - 이는 DI, OCP 원칙을 지킬 수 있는 좋은 점이지만, 반대로 구조가 복잡해지면서 어댑터 코드와 실제 코드가지
     * - 함께 유지보수 해야 하는 어려움도 발생한다
     *
     * 다른 선택
     * - 완전히히다른 선택으로 ItemService 코드 일부를 고쳐서 직접 SpringDataJpaItemRepository 를 사용하는 방법이다
     * - DI, OCP 원칙을 포기하는 대신에 복잡한 어댑터를 제거하고 구조를 단순하게 가져갈 수 있는 장점이 있다
     *
     * 트레이드 오프
     * - 위와 같은 상황을 트레이드 오프라고 한다
     * 1. DI, OCP 를 지키기 위해 어댑터를 도입하고 더 많은 코드를 유지한다
     * 2. 어댑터를 제거하고 구조를 단순하게 가져가지만, DI, OCP 를 포기한다
     * - 이는 구조의 안전성 VS 단순한 구조와 개발의 편리성의 차이
     * - 상황에 따라 더 나은 것을 선택해야 한다
     * - 어설픈 추상화는 오히려 독이되고, 추상화도 비용이 들기 때문에 이 추상화가 비용을 넘어설 만큼의
     * - 효과가 있을 때 추상화를 도입하는 것이 실용적이다
     */
    private final SpringDataJpaItemRepository repository;

    @Override
    public Item save(Item item) {
        return repository.save(item);
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = repository.findById(itemId).orElseThrow();
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        //조건 때문에 코드가 지저분해져 보기 불편하다 - QueryDsl 로 해결해보자
        if(StringUtils.hasText(itemName) && maxPrice != null){
            return repository.findItems("%" + itemName + "%", maxPrice);
        }else if(StringUtils.hasText(itemName)){
            return repository.findByItemNameLike("%" + itemName + "%");
        }else if(maxPrice != null){
            return repository.findByPriceLessThanEqual(maxPrice);
        }else {
            return repository.findAll();
        }
    }
}
