package com.revature.services;

import com.revature.dtos.ProductReviewRequest;
import com.revature.models.Product;
import com.revature.models.ProductReview;
import com.revature.models.User;
import com.revature.repositories.ProductRepository;
import com.revature.repositories.ProductReviewRepository;
import com.revature.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductReviewTest {
    @Mock
    private ProductReviewRepository productReviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Autowired
    @InjectMocks
    ProductReviewService productReviewService;
    @Mock
    ProductService productService;
    @Mock
    UserService userService;
    public List<ProductReview> productReviewMock;
    public User userMock;
    public Product productMock;

    @BeforeEach // Every thing inside setup will start before each method
    void setUp() {
        userService=new UserService(userRepository);

        productService= new ProductService(productRepository);

        productReviewService= new ProductReviewService(productReviewRepository,userService,productService);
        // Mock data
        productReviewMock = new ArrayList<>();
        productMock = new Product(101, 2, 5, "good", "img", "tomato", true);
        userMock = new User(201, "email", "pwd", "fname", "lname", false, true, "Authorization");
        productReviewMock.add(new ProductReview(1,3,"not bad", productMock, userMock));
        productReviewMock.add(new ProductReview(2,5,"very good", productMock, userMock));
    }

    @AfterEach  // After each method run Mock product review will become null
    void tearDown() {
        productReviewMock=null;
    }

    @DisplayName("Find all product reviews")
    @Test
    void findAll() {
        when(productReviewRepository.findAll()).thenReturn(productReviewMock);
        assertEquals(2,productReviewService.findAll().size());
    }
    @DisplayName("Find product review by id")
    @Test
    void findById() {
        when(productReviewRepository.findById(1)).thenReturn(Optional.of(productReviewMock.get(0)));
        assertTrue(productReviewService.findById(1).isPresent());
    }

    @DisplayName("Find product review by product id")
    @Test
    void findByProductId() {
        when(productReviewRepository.findAllByProductId(101)).thenReturn(productReviewMock);
        assertEquals(2, productReviewService.findByProductId(101).size());
    }
    @DisplayName("Find product review average score")
    @Test
    void findProductAverageScore() {
        when(productReviewRepository.findProductAverageScore(101)).thenReturn(Arrays.asList(4,6));
        List<Integer> list = new ArrayList<>(productReviewRepository.findProductAverageScore(101));
        int avg = list.stream().reduce(0, Integer::sum)/list.size();
        assertEquals(avg, productReviewService.findProductAverageScore(101));
    }
    @DisplayName("Find product review average score - negative testing")
    @Test
    void findProductAverageScore_withListSizeZero() {
        when(productReviewRepository.findProductAverageScore(102)).thenReturn(Arrays.asList());
        List<Integer> list = new ArrayList<>(productReviewRepository.findProductAverageScore(102));
        assertEquals(list.size(), productReviewService.findProductAverageScore(101));
        assertEquals(0, list.size());
    }

    @DisplayName("Find product review by product id and rating")
    @Test
    void findProductByScore() {
        when(productReviewRepository.findAllByProductScore(101, 5)).thenReturn(productReviewMock);
        assertEquals(2, productReviewService.findProductByScore(101, 5).size());
    }
    @DisplayName("User id with product id can post product review")
    @Test
    void canPostProductReview() {
        when(productReviewRepository.canPost(101, 201)).thenReturn(productReviewMock);
        assertFalse(productReviewService.canPost(101, 201));
    }
    @DisplayName("Updating product review")
    @Test
    void saveNewProductReview() {
        // Mock data
        Product product = new Product(102, 2, 5, "perishable", "img", "tomato", true);
        ProductReviewRequest prr = new ProductReviewRequest(1,4,"good",102);
        User user1 = new User(202, "email", "pwd", "fname", "lname", false, true, "valid");
        ProductReview pr = new ProductReview(102, 4, "good", product,user1);

        //When
        when(productRepository.findActiveById(102)).thenReturn(Optional.of(product));
        when(productReviewRepository.save(new ProductReview(prr, product, user1))).thenReturn(pr);

        //Then
        ProductReview p = productReviewService.save(prr, user1);
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
        assertEquals(4, p.getRating());

    }
    @DisplayName("Throw NullPointerException if rating > 5/comment=null")
    @Test
    void doNotSaveProductReviewRatingMoreThan5orNillComment() {
        lenient().when(productReviewRepository.save(any(ProductReview.class)))
                .thenReturn(new ProductReview()).thenThrow(NullPointerException.class);
        ProductReview p = productReviewService.save(new ProductReviewRequest(1, 7, "", 101), new User());
        assertNull(p);
    }

    @DisplayName("Delete a product review by id")
    @Test
    void deleteProductReviewById() {
        productReviewService.deleteById(101);
        verify(productReviewRepository, times(1)).deleteById(101);
    }
}