package hexlet.code.filter;

import hexlet.code.component.JWTUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static hexlet.code.config.security.SecurityConfig.DEFAULT_AUTHORITIES;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;

public class JWTAuthorizationFilter extends OncePerRequestFilter {
    private static final String BEARER = "Bearer";
    private final RequestMatcher publicUrls;
    private final JWTUtils jwtUtils;

    public JWTAuthorizationFilter(RequestMatcher publicUrls, JWTUtils jwtUtils) {
        this.publicUrls = publicUrls;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return publicUrls.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final var authToken = Optional.ofNullable(request.getHeader(AUTHORIZATION))
                .map(header -> header.replaceFirst("^" + BEARER, ""))
                .map(String::trim)
                .map(jwtUtils::readJWSToken)
                .map(claims -> claims.get(SPRING_SECURITY_FORM_USERNAME_KEY))
                .map(Object::toString)
                .map(this::buildAuthToken)
                .orElseThrow();

//        final String header = request.getHeader(AUTHORIZATION);
//        final String token = header.replaceFirst("^Bearer", "").trim();
//        final String username = jwtUtils.readJWSToken(token)
//                .get(SPRING_SECURITY_FORM_USERNAME_KEY)
//                .toString();
//        final var authToken = new UsernamePasswordAuthenticationToken(
//                username,
//                null,
//                DEFAULT_AUTHORITIES
//        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken buildAuthToken(final String username) {
        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                DEFAULT_AUTHORITIES
        );
    }
}
