package hello.itemservice.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "item") //객체 명과 같으면 생략 가능
public class Item {

    @Id //테이블의 PK를 해당 필드와 매핑
    @GeneratedValue(strategy = GenerationType.IDENTITY) //PK 생성 값을 데이터베이스의 IDENTITY 방식 사용
    private Long id;

    @Column(name = "item_name", length = 10) //객체의 필드와 테이블의 컬럼을 매핑
    private String itemName;

    //생략할 경우 필드의 이름을 테이블 컬럼 이름으로 사용한다. 스네이크 > 카멜 케이스 자동 변환 지원
    private Integer price;
    private Integer quantity;

    public Item() { //JPA 는 public 또는 protected 기본 생성자가 필수
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
