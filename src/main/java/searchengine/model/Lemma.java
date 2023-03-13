package searchengine.model;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "lemma", uniqueConstraints = {
        @UniqueConstraint(name = "uc_index_lemma", columnNames = {"lemma", "site_id"})
})

public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    String lemma;

    @ManyToOne( fetch = FetchType.EAGER, optional = false)
    @JoinColumn( name = "site_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site site;

    int frequency;
}
