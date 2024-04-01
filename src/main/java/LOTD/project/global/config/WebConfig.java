package LOTD.project.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000","http://52.79.55.124:3000","http://ec2-43-203-215-137.ap-northeast-2.compute.amazonaws.com:3000")
                .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.HEAD.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name()
                )
                .allowedHeaders("*")
                .exposedHeaders("Authorization","Authorization-refresh")
                .allowCredentials(true) // 쿠키 인증 요청 허용
                .maxAge(3000) ;// 원하는 시간만큼 pre-flight 리퀘스트를 캐싱

    }
}