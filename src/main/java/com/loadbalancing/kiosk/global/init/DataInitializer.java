package com.loadbalancing.kiosk.global.init;

import com.loadbalancing.kiosk.domain.admin.infra.entity.Admin;
import com.loadbalancing.kiosk.domain.admin.infra.repository.AdminRepository;
import com.loadbalancing.kiosk.domain.product.infra.entity.Product;
import com.loadbalancing.kiosk.domain.product.infra.entity.ProductImg;
import com.loadbalancing.kiosk.domain.product.infra.repository.ProductImgRepository;
import com.loadbalancing.kiosk.domain.product.infra.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// dev/test 프로필에서 서버 뜰 때 상품 4개(썸네일+이미지 3장씩) + 관리자 계정 하나 자동 삽입
// test 프로필도 포함시킨 이유: 테스트가 이 초기 데이터를 기준으로 돌아가야 해서 (H2에도 동일하게 시딩됨)
// Order/OrderItem 목데이터는 그쪽 담당자가 자기 Repository 만들면서 추가할 예정이라 여기선 안 건드림
@Profile({"dev", "test"})
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ProductImgRepository productImgRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) return; // 재시작마다 중복 삽입 방지

        createProduct(
                "에티오피아 예가체프", "꽃향과 산미가 조화로운 원두",
                18000, 50, "yirgacheffe"
        );
        createProduct(
                "콜롬비아 수프리모", "균형 잡힌 바디감과 은은한 단맛",
                16000, 40, "colombia"
        );
        createProduct(
                "케냐 AA", "진한 산미와 와인 같은 풍미",
                19000, 30, "kenya"
        );
        createProduct(
                "브라질 산토스", "고소하고 묵직한 다크 초콜릿 향",
                15000, 60, "brazil"
        );

        adminRepository.save(Admin.builder()
                .adminId("admin01")
                .password(passwordEncoder.encode("admin1234!"))
                .name("관리자")
                .build());
    }

    // seed별로 다른 실제 사진이 로딩되는 무료 placeholder(Picsum) 사용
    // 상품마다 썸네일 1장 + productImg 3장(seed-1~3)
    private void createProduct(String title, String description, int price, int stock, String seed) {
        Product product = productRepository.save(Product.builder()
                .title(title)
                .description(description)
                .price(price)
                .stock(stock)
                .thumbnail(picsum(seed))
                .build());

        for (int i = 1; i <= 3; i++) {
            productImgRepository.save(ProductImg.builder()
                    .product(product)
                    .url(picsum(seed + "-" + i))
                    .build());
        }
    }

    private String picsum(String seed) {
        return "https://picsum.photos/seed/" + seed + "/400/400";
    }
}
