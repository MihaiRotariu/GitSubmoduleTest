package com.qplayer.common.security;

import java.net.HttpCookie;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

/**
 * Authentication utils that can be shared between Q modules. Has no dependencies on specific Q services.
 * @author Mircea Nagy
 *
 */
@Configuration
public class CommonTokenAuthenticationUtils {

	static final long EXPIRATIONTIME = TimeUnit.DAYS.toMillis(10);
	public static final String TOKEN_PREFIX = "Bearer";
	public static final String HEADER_STRING = "Authorization";
	public static final String PARAM_STRING = "q-token";
	public static final String AUTHORITIES_KEY = "permissions";
	public static final String COURSES_KEY = "courses";

	private static String APP_SECRET;

	@Value("${app.secret}")
	public void setSecret(String secret) {
		APP_SECRET = secret;
	}

	private static String getSecret() {
		return APP_SECRET;
	}

	/**
	 * Encodes a new JWT token, with the given authentication data, on the given http response.
	 * 
	 * @param res the http response on which to encode the JWT token (as a header).
	 * @param authentication the authentication that will be stored in the JWT token.
	 */
	public static void addAuthentication(HttpServletResponse res, Authentication authentication) {
		String auth = createQJwtFromAuthentication(authentication);
		res.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + auth);
		res.addCookie(createAuthenticationCookie(auth));
	}

	public static void removeAuthentication(HttpServletResponse res) {
		Cookie authCookie = createAuthenticationCookie("");
		authCookie.setMaxAge(0);
		res.addCookie(authCookie);
	}

	private static Cookie createAuthenticationCookie(String auth) {
		Cookie authCookie = new Cookie(HEADER_STRING, auth);
		authCookie.setPath("/");
		authCookie.setHttpOnly(true);
		authCookie.setSecure(true);
		return authCookie;
	}

	/**
	 * Creates a JWT token, with the given authentication data.
	 * @param authentication the authentication that will be stored in the JWT token.
	 */
	public static String createQJwtFromAuthentication(Authentication authentication) {
		List<Long> accessibleLevelIds = extractAccessibleLevelIdsFromAuthentication(authentication);
		return createQJwtFromParameters(authentication.getName(), convertAuthoritiesToString(authentication.getAuthorities()), accessibleLevelIds);
	}

	public static String createQJwtFromPermissionsAndLevels(String userName, Collection<Permissions> permissions, List<Long> accessibleLevelIds) {
		return createQJwtFromParameters(userName, convertPermissionsToString(permissions), accessibleLevelIds);
	}
	
	private static String createQJwtFromParameters(String userName,
			String authorities, List<Long> accessibleLevelIds) {
		JwtBuilder builder = Jwts.builder()
        .setSubject(userName)
        .claim(AUTHORITIES_KEY, authorities);
		
		if (!accessibleLevelIds.isEmpty()) {
			builder.claim(COURSES_KEY, convertCoursesToString(accessibleLevelIds));
		}
        
	    String JWT = builder
				.setIssuedAt(Date.from(Instant.now()))
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
				.setId(new Random().toString())
				.signWith(SignatureAlgorithm.HS512, getSecret())
				.compact();
		return JWT;
	}
	  
	private static String convertAuthoritiesToString(Collection<? extends GrantedAuthority> receivedAuthorities) {
		return receivedAuthorities.stream().map(authority -> authority.getAuthority()).collect(Collectors.joining(","));
	}
	
	private static String convertPermissionsToString(Collection<Permissions> receivedPermissions) {
		return receivedPermissions.stream().map(permission -> permission.toString()).collect(Collectors.joining(","));
	}
	
	public static String convertCoursesToString(List<Long> accessibleLevelIds) {
		return accessibleLevelIds.stream().map(level -> level.toString()).collect(Collectors.joining(","));
	}
	  
	/**
	 * Extracts the Authentication object from a http request.
	 * 
	 * @param request http request that should have the JWT token in its Authorization header.
	 * @return null if the JWT token cannot be parsed; a valid Authentication
	 *         object with the user name otherwise.
	 */
	public static Authentication getAuthentication(HttpServletRequest request) {
		return getAuthenticationFromToken(getAuthenticationFromRequest(request));
	}

	public static String getAuthenticationFromRequest(HttpServletRequest request) {
		String authHeader = request.getHeader(HEADER_STRING);
		if (!StringUtils.isEmpty(authHeader)) {
			return authHeader;
		}
		Cookie cookie = WebUtils.getCookie(request, HEADER_STRING);
		if (cookie != null) {
			return TOKEN_PREFIX + " " + cookie.getValue();
		}
		return request.getParameter(PARAM_STRING);
	}

	public static Authentication getAuthenticationFromToken(String token) {
		if (token == null) {
			return null;
		}
		if (isQToken(token)) {
			return parseQToken(removePrefix(token, TOKEN_PREFIX));
		}
		return null;
	}

	public static Jws<Claims> extractQJwsFromRequest(HttpServletRequest request) {
		String authenticationHeader = CommonTokenAuthenticationUtils.getAuthenticationFromRequest(request);
		if (CommonTokenAuthenticationUtils.isQToken(authenticationHeader)) {
			return CommonTokenAuthenticationUtils.getJws(removePrefix(authenticationHeader, CommonTokenAuthenticationUtils.TOKEN_PREFIX));
		}
		return null;
	}

	private static String removePrefix(String token, String tokenPrefix) {
		return token.replace(tokenPrefix, "").trim();
	}

	public static boolean isQToken(String token) {
		return token.startsWith(TOKEN_PREFIX);
	}

	private static Authentication parseQToken(String token) {
		Jws<Claims> jws = getJws(token);
		String user = parseUserFromJwtTokenHeaderString(jws);
		if (user == null) {
			return null;
		}
		
		Collection<? extends GrantedAuthority> authorities = parseAuthoritiesFromJwtToken(jws);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
		addAccessibleLevelIdsToAuthentication(authentication, parseCoursesFromJwtTokenHeaderString(jws));
		return authentication;
	}

	private static Jws<Claims> getJws(String token) {
		return Jwts.parser().setSigningKey(getSecret()).parseClaimsJws(token);
	}

	public static void addAccessibleLevelIdsToAuthentication(AbstractAuthenticationToken auth, List<Long> levelIds) {
		if (!levelIds.isEmpty()) {
			AuthenticationDetails authDetails = new AuthenticationDetails();
			authDetails.addAccessibleLevelId(levelIds);
			auth.setDetails(authDetails);
		}
	}
	
	public static List<Long> extractAccessibleLevelIdsFromAuthentication(Authentication auth) {
		List<Long> result = new ArrayList<>();
		Object authDetails = auth.getDetails();
		if (authDetails instanceof AuthenticationDetails) {
			result.addAll(((AuthenticationDetails) authDetails).getAccessibleLevelIds());
		}
		return result;
	}

	public static String parseUserFromJwtTokenHeaderString(Jws<Claims> jws) {
		return jws.getBody().getSubject();
	}

	public static Collection<? extends GrantedAuthority> parseAuthoritiesFromJwtToken(Jws<Claims> jws) {
		return convertToAuthorityList(jws.getBody().get(AUTHORITIES_KEY).toString());
	}
	
	public static List<Long> parseCoursesFromJwtTokenHeaderString(Jws<Claims> jws) {
		List<Long> result = Collections.emptyList();
		Object courseIds = jws.getBody().get(COURSES_KEY);
		if (courseIds != null) {
			result = convertToCourseList(courseIds.toString());
		}
		return result;
	}

	private static List<SimpleGrantedAuthority> convertToAuthorityList(String authoritiesString) {
		return Arrays.asList(authoritiesString.split(",")).stream().filter(authority -> !(authority == null) && !authority.isEmpty())
				.map(authority -> new SimpleGrantedAuthority(authority)).collect(Collectors.toList());
	}
	
	public static List<Long> convertToCourseList(String coursesString) {
		return Arrays.asList(coursesString.split(",")).stream().filter(course -> !(course == null) && !course.isEmpty())
				.map(course -> Long.valueOf(course)).collect(Collectors.toList());
	}
	
}