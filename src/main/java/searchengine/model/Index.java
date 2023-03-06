package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "indexed", uniqueConstraints = {
        @UniqueConstraint(name = "uc_index_lemma_id", columnNames = {"lemma_id", "page_id"})
})

public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    private int ranks;


    @ManyToOne ( fetch = FetchType.EAGER, optional = false)
    @JoinColumn( name = "lemma_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lemma lemma;


    @ManyToOne( fetch = FetchType.EAGER, optional = false)
    @JoinColumn( name = "page_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Page page;



}
