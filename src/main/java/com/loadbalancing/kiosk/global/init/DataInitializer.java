package com.loadbalancing.kiosk.global.init;

import com.loadbalancing.kiosk.domain.admin.entity.Admin;
import com.loadbalancing.kiosk.domain.admin.repository.AdminRepository;
import com.loadbalancing.kiosk.domain.product.entity.Product;
import com.loadbalancing.kiosk.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// dev 프로필에서 서버 뜰 때 상품 4개 + 관리자 계정 하나 자동 삽입
// Order/OrderItem 목데이터는 그쪽 담당자가 자기 Repository 만들면서 추가할 예정이라 여기선 안 건드림
@Profile("dev")
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) return; // 재시작마다 중복 삽입 방지

        productRepository.save(Product.builder()
                .title("에티오피아 예가체프")
                .description("꽃향과 산미가 조화로운 원두")
                .price(18000)
                .stock(50)
                .thumbnail("https://example.com/images/yirgacheffe.jpg")
                .build());

        productRepository.save(Product.builder()
                .title("콜롬비아 수프리모")
                .description("균형 잡힌 바디감과 은은한 단맛")
                .price(16000)
                .stock(40)
                .thumbnail("https://example.com/images/colombia.jpg")
                .build());

        productRepository.save(Product.builder()
                .title("케냐 AA")
                .description("진한 산미와 와인 같은 풍미")
                .price(19000)
                .stock(30)
                .thumbnail("https://example.com/images/kenya.jpg")
                .build());

        productRepository.save(Product.builder()
                .title("브라질 산토스")
                .description("고소하고 묵직한 다크 초콜릿 향")
                .price(15000)
                .stock(60)
                .thumbnail("https://example.com/images/brazil.jpg")
                .build());

        adminRepository.save(Admin.builder()
                .adminId("admin01")
                .password(passwordEncoder.encode("admin1234!"))
                .name("관리자")
                .build());
    }
}
