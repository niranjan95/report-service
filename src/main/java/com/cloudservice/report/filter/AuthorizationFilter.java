package com.cloudservice.report.filter;

import static org.springframework.http.HttpMethod.OPTIONS;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.cloudservice.report.config.SwaggerConfig;
import com.cloudservice.report.exception.UserNotFoundException;
import com.cloudservice.report.model.UserData;
import com.cloudservice.report.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

	protected static final List<String> PATHS_TO_SKIP = Arrays.asList("/swagger", "/v3/api-docs", "/health", "/actuator");
	
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {
        try {
    		String authorization = request.getHeader(SwaggerConfig.AUTHORIZATION);
        	if (isNotEmpty(authorization)) {
        		UserData user = authorizeUser(authorization);
        		request.setAttribute("clientId", user.getClientId());
        		filterChain.doFilter(request, response);
        	}else {
        	    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        	    log.error("Unauthorized Access");
        	}
        } catch (UserNotFoundException e) {
            log.error("Authorization Error", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"Unauthorized\": \"" + e.getMessage() + ".\"}");
        } catch (Exception e) {
            log.error("Server Error", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"Server error\": \"" + e.getMessage() + ".\"}");
        }
    }

    private UserData authorizeUser(String authorization) {
		UserData user = userRepository.findBySecretkey(authorization);
		if(user == null) {
			throw new UserNotFoundException("User not found with secretkey [" + authorization + "]");
		}
		return user;
	}

	@Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    	String path = request.getRequestURI();
    	return PATHS_TO_SKIP.stream().anyMatch(path::contains);
    }
}