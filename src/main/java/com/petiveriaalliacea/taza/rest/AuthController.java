package com.petiveriaalliacea.taza.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.petiveriaalliacea.taza.dto.AuthRequestDto;
import com.petiveriaalliacea.taza.dto.TokenDto;
import com.petiveriaalliacea.taza.dto.UserRequestDto;
import com.petiveriaalliacea.taza.entities.User;
import com.petiveriaalliacea.taza.security.JwtUtils;
import com.petiveriaalliacea.taza.services.impl.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Data
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(allowedHeaders = "*", origins = "*")
public class AuthController {

    @Autowired
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    @Autowired
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequestDto request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        final UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());
        if (authentication.isAuthenticated()){
            String accessToken = jwtUtils.generateToken(user);
            String refreshToken = jwtUtils.generateRefreshToken(user);
            return ResponseEntity.ok(new TokenDto(accessToken, refreshToken));
        }
        return ResponseEntity.status(400).body("Some error has occurred");
    }

    @GetMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(AUTHORIZATION);
        final String username;
        final String refreshToken;

        if(authHeader != null || authHeader.startsWith("Bearer ")) {
            refreshToken = authHeader.substring(7);
            username = jwtUtils.extractUsername(refreshToken);
            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtils.isTokenValid(refreshToken, userDetails)) {
                    String accessToken = jwtUtils.generateToken(userDetails);
                    TokenDto tokenDto = new TokenDto(accessToken, refreshToken);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(),tokenDto);
                    return ResponseEntity.ok(tokenDto);
                }
            }
        } return ResponseEntity.status(400).body("Some error has occurred");
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserRequestDto user){
        return ResponseEntity.ok().body(userService.register(user));
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AuthController;
    }
}
