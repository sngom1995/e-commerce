package com.example.demo.controllers;

import com.example.demo.model.AuthResponse;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.AuthRequest;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.security.GenerateJwtToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		return ResponseEntity.of(userRepository.findById(id));
	}
	
	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		log.info("Find user by Username {}", username);
		User user = userRepository.findByUsername(username);
		return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
	}
	
	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
		log.info("Create user {}", createUserRequest.getUsername());
		User user = new User();
		user.setUsername(createUserRequest.getUsername());
		Cart cart = new Cart();
		cartRepository.save(cart);
		user.setCart(cart);
		if(createUserRequest.getPassword().length()<7 ||
				!createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword())){
			log.error( "Error :Either length is less than 7 or pass and conf pass do not match. Unable to create ",
					createUserRequest.getUsername());
			log.info("User creation failure");
			return ResponseEntity.badRequest().build();
		}
		if (userRepository.findByUsername(user.getUsername()) != null) {
			log.info("User creation failure");
			return ResponseEntity.badRequest().build();
		}
		user.setPassword(bCryptPasswordEncoder.encode(createUserRequest.getPassword()));
		userRepository.save(user);
		log.info("User creation successful");
		return ResponseEntity.ok(user);
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
		log.info("Login user {}", authRequest.getUsername());
		User user = userRepository.findByUsername(authRequest.getUsername());
		if (user == null) {
			return ResponseEntity.status(401).build();
		}
		if (!bCryptPasswordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
			return ResponseEntity.status(401).build();
		}
		AuthResponse authResponse = new AuthResponse();
		String token = new GenerateJwtToken().generateToken(user);
		authResponse.setToken(token);
		return ResponseEntity.ok(authResponse);
	}
	
}
