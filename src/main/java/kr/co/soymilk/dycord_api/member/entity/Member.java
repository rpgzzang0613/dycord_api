//package kr.co.soymilk.dycord_api.member.entity;
//
//import jakarta.persistence.*;
//import jakarta.validation.constraints.NotNull;
//import org.springframework.data.annotation.CreatedDate;
//
//import java.time.LocalDateTime;
//
//@Entity
//public class Member {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotNull
//    @Column(nullable = false, unique = true)
//    private String email;
//
//    private String password;
//
//    private String nickname;
//
//    @CreatedDate
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @Column(nullable = false)
//    private boolean isDeleted;
//
//    private LocalDateTime deletedAt;
//
//}
