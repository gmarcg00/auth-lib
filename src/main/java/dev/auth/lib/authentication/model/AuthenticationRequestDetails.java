package dev.auth.lib.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticationRequestDetails implements Serializable {
    private String uri;
}
