package tut.dushyant.cafedemo.cafe.web.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * DTO for {@link tut.dushyant.cafedemo.cafe.db.entity.Customer}
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Customer implements Serializable {
    private Long id;
    private String name;
    private String email;

    public Customer(tut.dushyant.cafedemo.cafe.db.entity.Customer entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.email = entity.getEmail();
    }

    public tut.dushyant.cafedemo.cafe.db.entity.Customer toEntity() {
        tut.dushyant.cafedemo.cafe.db.entity.Customer entity = new tut.dushyant.cafedemo.cafe.db.entity.Customer();
        entity.setId(this.id);
        entity.setName(this.name);
        entity.setEmail(this.email);
        return entity;
    }

    public Customer setId(Long id) {
        this.id = id;
        return this;
    }

    public Customer setEmail(String email) {
        this.email = email;
        return this;
    }

    public Customer setName(String name) {
        this.name = name;
        return this;
    }
}
