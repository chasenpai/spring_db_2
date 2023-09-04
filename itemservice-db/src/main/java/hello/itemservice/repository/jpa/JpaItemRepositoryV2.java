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
