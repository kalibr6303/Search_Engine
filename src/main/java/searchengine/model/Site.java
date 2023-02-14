package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;


@Getter
@Setter
@Entity
@Table(name = "Site")
public class Site {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @Enumerated(EnumType.STRING)
    private StatusType status;

    @Column(name = "status_time")
    private Date statusTime;

    @Column(name = "last_error")
    private String lastError;

    private String url;

    private String name;

}
