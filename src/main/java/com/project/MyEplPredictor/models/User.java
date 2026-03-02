package com.project.MyEplPredictor.models;

import com.project.MyEplPredictor.DTO.UserDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Value;
//import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "app_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String email;

    @CreationTimestamp
    private LocalDateTime createdAt;

    /*
     * One user can participate in MANY leagues
     * through LeagueMember
    */

    @OneToMany(mappedBy = "user")
    private List<LeagueMember> leagueMemberships;

    public User(UserDto userDto) {

    }
}
