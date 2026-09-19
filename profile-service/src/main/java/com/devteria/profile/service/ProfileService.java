package com.devteria.profile.service;

import com.devteria.profile.dto.identity.Credential;
import com.devteria.profile.dto.identity.TokenExchangeParam;
import com.devteria.profile.dto.identity.UserCreationParam;
import com.devteria.profile.dto.request.RegistrationRequest;
import com.devteria.profile.dto.response.ProfileResponse;
import com.devteria.profile.mapper.ProfileMapper;
import com.devteria.profile.repository.ProfileRepository;
import com.devteria.profile.repository.httpclient.IdentityClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService {
    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    IdentityClient identityClient;
    @NonFinal
    @Value("${app.grant_type}")
    String grantType;
    @NonFinal @Value("${app.client_id}")
    String clientId;
    @NonFinal @Value("${app.client_secret}")
    String clientSecret;
    @NonFinal @Value("${app.scope}")
    String scope;
    public List<ProfileResponse> getAllProfiles(){
        var profiles = profileRepository.findAll();
        return profiles.stream().map(profileMapper::toProfileResponse).toList();
    }

    public ProfileResponse register(RegistrationRequest request){
        var token = identityClient.exchangeToken(TokenExchangeParam.builder()
                .grant_type(grantType)
                .client_id(clientId)
                .client_secret(clientSecret)
                .scope(scope)
                .build());
        log.info("Token info {}", token);

        var creationResponse = identityClient.createUser(
                "Bearer " + token.getAccessToken(),
                UserCreationParam.builder()
                        .username(request.getUsername())
                        .enabled(true)
                        .email(request.getEmail())
                        .emailVerified(false)
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .credentials(
                                List.of(Credential.builder()
                                        .type("password")
                                        .value(request.getPassword())
                                        .temporary(false)
                                        .build()
                                )
                        )
                        .build()
                );
        String userId = extractUserId(creationResponse);
        var profile = profileMapper.toProfile(request);
        profile.setUserId(userId);
        profile = profileRepository.save(profile);

        return profileMapper.toProfileResponse(profile);
    }
    private String extractUserId(ResponseEntity<?> response){
        String location = response.getHeaders().get("location").getFirst();
        String[] splitedStr = location.split("/");
        return splitedStr[splitedStr.length - 1];
    }
}
