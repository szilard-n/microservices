package com.example.productservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtConverter = new JwtGrantedAuthoritiesConverter();

    @Value("${jwt.auth.resource-id}")
    private String resourceId;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
                Objects.requireNonNull(jwtConverter.convert(jwt)).stream(),
                extractResourceRoles(jwt).stream()
        ).collect(Collectors.toSet());
        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaim(JwtClaimNames.SUB));
    }

    /**
     * Extracts the roles of the user from the JWT token based on the following json format:
     * <pre>
     *     "resource_access": {
     *      "access-token": {
     *          "roles": [
     *              "user",
     *              "admin"
     *          ]
     *      }
     * </pre>
     */
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        final String resourceAccessClaim = "resource_access";
        final String roles = "roles";

        if (!jwt.hasClaim(resourceAccessClaim)) {
            return Collections.emptySet();
        }

        Map<String, Object> resourceAccess = jwt.getClaim(resourceAccessClaim);

        if (resourceAccess.get(resourceId) == null) {
            return Collections.emptySet();
        }

        Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(resourceId);

        if (resource.get(roles) == null) {
            return Collections.emptySet();
        }

        Collection<String> resourceRole = (Collection<String>) resource.get(roles);

        return resourceRole.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
