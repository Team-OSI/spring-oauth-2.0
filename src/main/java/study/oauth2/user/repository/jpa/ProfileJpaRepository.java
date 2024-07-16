package study.oauth2.user.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import study.oauth2.user.domain.entity.Profile;

public interface ProfileJpaRepository extends JpaRepository<Profile, Long>{

	boolean existsByNickname(String nickname);
}
