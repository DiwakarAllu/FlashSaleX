package com.flashsalex.search;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.flashsalex.search.repository.ProductSearchRepository;

@SpringBootTest
class SearchSvcApplicationTests {

	@MockitoBean
	private ProductSearchRepository productSearchRepository;

	@Test
	void contextLoads() {
	}

}
