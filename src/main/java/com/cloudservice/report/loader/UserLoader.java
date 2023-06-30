package com.cloudservice.report.loader;

import com.cloudservice.report.model.UserData;
import com.cloudservice.report.repository.UserRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserLoader {
	private static final String FILE_PATH = "userData.csv";
	private UserRepository userRepository;
	
	public UserLoader(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@PostConstruct
	public void loadUsersFromFile() {
		ClassPathResource resource = new ClassPathResource(FILE_PATH);
		try (BufferedReader reader = new BufferedReader(new FileReader(resource.getFile()))) {
			String line;
			boolean isFirstLine = true;
			String[] headers = null;

			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				if (isFirstLine) {
					headers = parts;
					isFirstLine = false;
				} else if (headers != null && parts.length == headers.length) {
					Map<String, String> attributes = new HashMap<>();

					for (int i = 0; i < headers.length; i++) {
						String header = headers[i].trim();
						String value = parts[i].trim();
						attributes.put(header, value);
					}

					UserData user = createUserFromAttributes(attributes);
					if (user != null) {
						userRepository.save(user);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private UserData createUserFromAttributes(Map<String, String> attributes) {
		String secretkey = attributes.get("secretkey");
		String clientId = attributes.get("clientId");

		// Validate required attributes
		if (secretkey != null  && clientId != null) {
			return new UserData(secretkey, clientId);
		}

		// Invalid user attributes
		return null;
	}
}