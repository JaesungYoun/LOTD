package LOTD.project.global.login.service.redis;

import LOTD.project.global.jwt.JwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate redisTemplate;


    /**
     * @author 윤재성
     * @date 2024-01-10
     * @description : 로그아웃 시 블랙리스트에 AT저장
     * @return
     */
    public void addToBlacklist(String accessToken) {
        redisTemplate.opsForSet().add("accessToken:blackList", accessToken);
    }


    /**
     * refresh token 저장
     * @param memberId
     * @param refreshToken
     */
    public void setRefreshToken(String memberId, String refreshToken) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(memberId, refreshToken, Duration.ofDays(14)); // 2주 저장 설정
    }

    /**
     * refresh token 가져오기
     * @param memberId
     * @return
     */
    public String getRefreshToken(String memberId) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        return values.get(memberId);
    }

    /**
     * refresh token 삭제 
     * @param memberId
     */
    public void delRefreshToken(String memberId) {
        redisTemplate.delete(memberId);
    }

    public void getRedisblack() {
        SetOperations<String, Object> setOperations = redisTemplate.opsForSet();
    }
    public void deleteRedisElementsAll(){
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    public void deleteAccessTokenInBlackList(String accessToken) {
        if (redisTemplate.opsForSet().isMember("accessToken:blackList", accessToken)) {
            redisTemplate.opsForSet().remove("accessToken:blackList", accessToken);
        }
    }

    /**
     * @author 윤재성
     * @date 2024-01-10
     * @description : AT가 Redis의 블랙리스트에 저장되어있는지 확인, opsForSet을 통해 SET데이터에 접근하여 ACCESSTOKEN이 있는지 확인, 만료된 accesstoken은 블랙리스트에서 삭제
     * @return
     */
    public boolean isTokenInBlacklist(String accessToken, Long accessTokenExpired) {
        try {

            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("accessToken:blackList", accessToken));

        } catch (Exception e) {
            throw new RuntimeException("Failed to check token blacklist", e);
        }

    }
}



