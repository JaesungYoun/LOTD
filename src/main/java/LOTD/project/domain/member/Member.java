package LOTD.project.domain.member;



import LOTD.project.domain.heart.Heart;
import LOTD.project.domain.member.oauth2.SocialType;
import LOTD.project.global.audit.BaseEntity;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "member")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@Setter
public class Member extends BaseEntity implements UserDetails {

    @Id
    @Column(nullable = false, name = "MEMBER_ID", unique = true)
    private String memberId; // 회원 ID

    @Column(name = "PASSWORD")
    private String password; // 패스워드

    @Column(nullable = false, name = "NICKNAME", length = 20)
    private String nickname; // 닉네임

    @Column(name = "EMAIL")
    private String email; // 이메일

    @Column(name = "SOCIAL_TYPE")
    @Enumerated(EnumType.STRING)
    private SocialType socialType; // KAKAO, NAVER, GOOGLE

    @Column(name = "ROLE")
    @Enumerated(EnumType.STRING)
    private Role role;


    // 패스워드 암호화 메소드
    public void passwordEncode(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    // 패스워드 일치 확인 
    public boolean matchPassword(PasswordEncoder passwordEncoder, String checkPassword) {
        return passwordEncoder.matches(checkPassword, this.password);
    }

    // 비밀번호 변경
    public void changePassword(PasswordEncoder passwordEncoder, String password){
        this.password = passwordEncoder.encode(password);
    }

    // 닉네임 변경
    public void updateNickname(String nickname){
        this.nickname = nickname;
    }

    // 이메일 변경
    public void updateEmail(String email) { this.email = email; }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.name()));
        return authorities;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }


}
