package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import javax.persistence.*;
import javax.persistence.Index;


@Getter
@Setter
@Entity
@Table(name = "Page", indexes = {
        @Index(name = "idx_page_path", columnList = "path")
})



public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @Column(name = "path")
    private String path;

    private int code;


    @Column(length = 1000000)
    private String content;

    @ManyToOne( fetch = FetchType.EAGER, optional = false)
    @JoinColumn( name = "site_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)

    private Site site;


    public Page() {
    }



}
