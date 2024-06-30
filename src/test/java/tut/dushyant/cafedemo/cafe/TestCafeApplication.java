package tut.dushyant.cafedemo.cafe;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tut.dushyant.cafedemo.cafe.db.entity.Customer;
import tut.dushyant.cafedemo.cafe.db.repo.CustomerRepo;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("resource")
@Slf4j
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
public class TestCafeApplication {

    static PostgreSQLContainer<?> postgreSQLContainer;
    static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    CustomerRepo customerRepo;

    @Autowired
    private MockMvc mockMvc;

    static {
        postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("dushyantsopra/postgres:lcl-16").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("cafedb")
            .withUsername("admin")
            .withPassword("test123")
                .withReuse(true);
    }

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }

    @BeforeEach
    void beforeEach() {
        if (!postgreSQLContainer.isRunning() || !postgreSQLContainer.isHealthy()) {
            postgreSQLContainer.start();
        }
    }

    @DynamicPropertySource
    static void configProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Test
    @Order(1)
    public void contextLoads() {
    }

    @Test
    @DisplayName("Should return all added customers")
    @Order(2)
    public void shouldReturnAllAddedCustomers() throws Exception {
        List<Customer> customers = List.of(
                new tut.dushyant.cafedemo.cafe.web.data.Customer().setName("Alice").setEmail("alice@cust.com").toEntity(),
                new tut.dushyant.cafedemo.cafe.web.data.Customer().setName("Bob").setEmail("bob@cust.com").toEntity()
        );
        customerRepo.saveAll(customers);

        this.mockMvc.perform(get("/customers"))
                .andDo(MockMvcResultHandlers.log())
                .andExpect(status().isOk())
                .andDo(result -> objectMapper.readValue(
                        result.getResponse().getContentAsByteArray(),
                        new TypeReference<List<Customer>>() {})
                .forEach(customer -> {
                    assert customer.getName().equals("Alice") || customer.getName().equals("Bob");
                }));
    }

    @Test
    @DisplayName("Should return empty customers")
    @Order(3)
    public void shouldReturnEmptyCustomers() throws Exception {
        customerRepo.deleteAll();

        this.mockMvc.perform(get("/customers"))
                .andDo(MockMvcResultHandlers.log())
                .andExpect(status().isOk()).andDo(result -> {
                    assert objectMapper.readValue(
                            result.getResponse().getContentAsByteArray(),
                            new TypeReference<List<Customer>>() {}).isEmpty();
                });
    }

    @Test
    @DisplayName("Should add customer")
    @Order(4)
    public void shouldAddCustomer() throws Exception {
        this.mockMvc.perform(post("/customers").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"name\":\"Charlie\",\"email\":\"test1@test.com\"}")).andDo(MockMvcResultHandlers.log())
                .andExpect(status().isOk()).andDo(result -> {
                    Customer customer = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Customer.class);
                    assert customer.getName().equals("Charlie");
                    assert customer.getEmail().equals("test1@test.com");
                });
    }

    @Test
    @DisplayName("Should throw exception when adding customer with duplicate email")
    @Order(5)
    public void shouldThrowExceptionWhenAddingCustomerWithDuplicateEmail() throws Exception {
        this.mockMvc.perform(post("/customers").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"name\":\"Charlie\",\"email\":\"test1@test.com\"}")).andDo(MockMvcResultHandlers.log())
                .andExpect(status().isOk()).andDo(result -> {
                    Customer customer = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Customer.class);
                    assert customer.getName().equals("Charlie");
                    assert customer.getEmail().equals("test1@test.com");
                    mockMvc.perform(post("/customers").contentType(MediaType.APPLICATION_JSON_VALUE)
                                    .content("{\"name\":\"Charlie\",\"email\":\"test1@test.com\"}")).andDo(MockMvcResultHandlers.log())
                            .andExpect(status().isConflict());
                });
    }

    @Test
    @DisplayName("Should Update customer")
    @Order(6)
    public void shouldUpdateCustomer() throws Exception{
        this.mockMvc.perform(post("/customers").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"name\":\"Charlie\",\"email\":\"test1@test.com\"}")).andDo(MockMvcResultHandlers.log())
                .andDo(result -> {
                    Customer customer = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Customer.class);
                    log.info("Customer: {}", customer);
                    assert customer.getName().equals("Charlie");
                    assert customer.getEmail().equals("test1@test.com");
                    mockMvc.perform(put("/customers").contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content("{\"id\":" + customer.getId() + ",\"name\":\"Charlie\",\"email\":\"test12@email.com\"}"))
                            .andExpect(status().isOk()).andDo(result1 -> {
                                Customer updatedCustomer = objectMapper.readValue(result1.getResponse().getContentAsByteArray(), Customer.class);
                                log.info("Updated Customer: {}", updatedCustomer);
                                assert updatedCustomer.getName().equals("Charlie");
                                assert updatedCustomer.getEmail().equals("test12@email.com");
                            });
                    });
    }

    @Test
    @DisplayName("Should Delete customer")
    @Order(7)
    public void shouldDeleteCustomer() throws Exception{
        this.mockMvc.perform(post("/customers").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"name\":\"Charlie\",\"email\":\"test1@test.com\"}")).andDo(MockMvcResultHandlers.log())
                .andDo(result -> {
                    Customer customer = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Customer.class);
                    log.info("Customer: {}", customer);
                    assert customer.getName().equals("Charlie");
                    assert customer.getEmail().equals("test1@test.com");
                    mockMvc.perform(delete("/customers/"+customer.getId())).andDo(MockMvcResultHandlers.log())
                            .andExpect(status().isOk()).andDo(result1 -> {
                                Customer updatedCustomer = objectMapper.readValue(result1.getResponse().getContentAsByteArray(), Customer.class);
                                log.info("Deleted Customer: {}", updatedCustomer);
                            });
                });
    }

    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
    }
}