package hello.springtx.propagation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Log {

    @Id
    @GeneratedValue
    private Long id;

    private String msg;

    public Log(String msg) {
        this.msg = msg;
    }

}
