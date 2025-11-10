package com.fitlink.domain;

import com.fitlink.domain.enums.Interest;
import jakarta.persistence.*;

@Entity
public class Interests {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Interest interest;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // ?몃옒???ㅼ젙
    private Users user;

    public Interests() {

    }

    public Interests(Interest enumInterest, Users newUser) {
        this.interest = enumInterest;
        this.user = newUser;
        user.getInterests().add(this);
    }

}

