package com.loadbalancing.kiosk.global.R3;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

//Spring Boot가 실행될 때, R2와 통신할 수 있는 클라이언트(손님) 객체를 생성해 두는 설정
//Access Key, Secret Key 같은 민감정보는 프론트엔드에 노출되면 안 되므로 직접 통신
@Configuration
public class R2Config {

    @Value("${cloudflare.r2.account-id}")
    private String accountId;

    @Value("${cloudflare.r2.access-key}")
    private String accessKey;

    @Value("${cloudflare.r2.secret-key}")
    private String secretKey;

    //S3Client? -> AWS가 제공하는 S3 스토리지와 통신하기 위한 전용 도구(리모컨, R2랑 100% 호환됨)
    @Bean
    public S3Client s3Client(){
        //R2에 접근하게 하는 객체를 만듬
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        //위의 credential 객체는 기본 좌표가 AWS url이기 때문에 R2 엔드포인트로 설정
        String endpoint = String.format("https://%s.r2.cloudflarestorage.com", accountId);

        //설정 완성
        return S3Client.builder()
                //클라이언트에 접근 객체 등록
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                //가까운 지역으로 자동연결
                .region(Region.of("auto"))
                .endpointOverride(URI.create(endpoint))
                .build();
    }
}
