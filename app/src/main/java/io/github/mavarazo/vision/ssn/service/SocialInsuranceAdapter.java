package io.github.mavarazo.vision.ssn.service;


import io.github.mavarazo.vision.user.model.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface SocialInsuranceAdapter {

    @GetExchange("/v1/social-insurances/{ssn}")
    User getUserBySocialInsuranceNo(@PathVariable final String ssn);
}
