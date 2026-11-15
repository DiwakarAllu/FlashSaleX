package com.flashsalex.user;

import com.jayway.jsonpath.JsonPath;
import com.flashsalex.user.repository.UserAddressRepository;
import com.flashsalex.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserApiIntegrationTest {

	private static final String SIGNUP = """
		{"name":"Ada Lovelace","email":"ada@example.com","password":"a-long-unique-password"}
		""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserAddressRepository addressRepository;

	@BeforeEach
	void clearDatabase() {
		addressRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void signupLoginAndProfileUseHashedPasswordAndJwtIdentity() throws Exception {
		mockMvc.perform(post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(SIGNUP))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.email").value("ada@example.com"))
			.andExpect(jsonPath("$.passwordHash").doesNotExist());

		String passwordHash = userRepository.findByEmail("ada@example.com").orElseThrow().getPasswordHash();
		assertNotEquals("a-long-unique-password", passwordHash);
		assertTrue(passwordHash.startsWith("$2"));

		String token = login("ADA@example.com", "a-long-unique-password");
		mockMvc.perform(get("/users/profile")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("ada@example.com"));
	}

	@Test
	void protectedRoutesRequireJwtAndAddressesAreOwnerScoped() throws Exception {
		mockMvc.perform(get("/users/profile"))
			.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/users/signup").contentType(MediaType.APPLICATION_JSON).content(SIGNUP))
			.andExpect(status().isCreated());
		String ownerToken = login("ada@example.com", "a-long-unique-password");

		MvcResult addressResult = mockMvc.perform(post("/users/addresses")
				.header("Authorization", "Bearer " + ownerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(address("1 Main Street", true)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.isDefault").value(true))
			.andReturn();
		String addressId = JsonPath.read(addressResult.getResponse().getContentAsString(), "$.addressId").toString();

		mockMvc.perform(post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Grace Hopper","email":"grace@example.com","password":"another-long-password"}
					"""))
			.andExpect(status().isCreated());
		String otherToken = login("grace@example.com", "another-long-password");

		mockMvc.perform(get("/users/addresses")
				.header("Authorization", "Bearer " + otherToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isEmpty());

		mockMvc.perform(put("/users/addresses/" + addressId)
				.header("Authorization", "Bearer " + otherToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(address("Changed Street", false)))
			.andExpect(status().isNotFound());

		mockMvc.perform(delete("/users/addresses/" + addressId)
				.header("Authorization", "Bearer " + otherToken))
			.andExpect(status().isNotFound());
	}

	@Test
	void profileAndOwnedAddressesCanBeUpdatedAndDeleted() throws Exception {
		mockMvc.perform(post("/users/signup").contentType(MediaType.APPLICATION_JSON).content(SIGNUP))
			.andExpect(status().isCreated());
		String token = login("ada@example.com", "a-long-unique-password");

		mockMvc.perform(put("/users/profile")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Ada Byron","email":"ada.byron@example.com"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Ada Byron"))
			.andExpect(jsonPath("$.email").value("ada.byron@example.com"));

		MvcResult firstAddressResult = mockMvc.perform(post("/users/addresses")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(address("1 Main Street", true)))
			.andExpect(status().isCreated())
			.andReturn();
		String firstAddressId = JsonPath.read(
			firstAddressResult.getResponse().getContentAsString(),
			"$.addressId"
		).toString();

		MvcResult secondAddressResult = mockMvc.perform(post("/users/addresses")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(address("2 Main Street", false)))
			.andExpect(status().isCreated())
			.andReturn();
		String secondAddressId = JsonPath.read(
			secondAddressResult.getResponse().getContentAsString(),
			"$.addressId"
		).toString();

		mockMvc.perform(put("/users/addresses/" + secondAddressId)
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(address("2 New Street", true)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isDefault").value(true));

		mockMvc.perform(get("/users/addresses")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].isDefault").value(false))
			.andExpect(jsonPath("$[1].addressLine").value("2 New Street"))
			.andExpect(jsonPath("$[1].isDefault").value(true));

		mockMvc.perform(delete("/users/addresses/" + firstAddressId)
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isNoContent());
	}

	@Test
	void duplicateEmailIsRejectedWithoutRevealingCredentials() throws Exception {
		mockMvc.perform(post("/users/signup").contentType(MediaType.APPLICATION_JSON).content(SIGNUP))
			.andExpect(status().isCreated());
		mockMvc.perform(post("/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Another Ada","email":"ADA@example.com","password":"different-long-password"}
					"""))
			.andExpect(status().isConflict());
		mockMvc.perform(post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"ada@example.com","password":"wrong-password"}
					"""))
			.andExpect(status().isUnauthorized());
	}

	private String login(String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"%s","password":"%s"}
					""".formatted(email, password)))
			.andExpect(status().isOk())
			.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
	}

	private String address(String line, boolean isDefault) {
		return """
			{"addressLine":"%s","city":"London","state":"London","postalCode":"SW1A 1AA","country":"UK","isDefault":%s}
			""".formatted(line, isDefault);
	}
}
