package com.gamingshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);

			if (jwtUtil.validateToken(token)) {
				String username = jwtUtil.extractUsername(token);
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				// ── Lire les rôles directement depuis le token ──
				Claims claims = jwtUtil.extractClaims(token);
				List<String> roles = claims.get("roles", List.class);

				List<SimpleGrantedAuthority> authorities;
				if (roles != null && !roles.isEmpty()) {
					// Utiliser les rôles du token
					authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
				} else {
					// Fallback : utiliser les authorities de UserDetails
					authorities = userDetails.getAuthorities().stream()
							.map(a -> new SimpleGrantedAuthority(a.getAuthority())).collect(Collectors.toList());
				}

				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
						authorities);
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		}

		chain.doFilter(request, response);
	}
}